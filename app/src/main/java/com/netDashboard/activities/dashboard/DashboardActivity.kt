package com.netDashboard.activities.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.activities.dashboard.new_tile.NewTileActivity
import com.netDashboard.activities.dashboard.settings.DashboardSettingsActivity
import com.netDashboard.createToast
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.DashboardActivityBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.margins
import com.netDashboard.tile.TileGridLayoutManager
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toPx
import java.util.*


class DashboardActivity : AppCompatActivity() {
    private lateinit var b: DashboardActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    lateinit var dashboardTilesAdapter: TilesAdapter

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                foregroundService = s
                onServiceReady()
            }
        })

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        setupRecyclerView()

        //Set dashboard tag name
        b.ban.text = settings.dashboardTagName.uppercase(Locale.getDefault())

        b.edit.setOnClickListener {
            editOnClick()
        }

        b.duo.setOnClickListener {
            setOnClick()
            swapOnClick()
        }

        b.remove.setOnClickListener {
            removeOnClick()
        }

        b.add.setOnClickListener {
            addOnClick()
        }
    }

    override fun onResume() {
        super.onResume()

        if (dashboardTilesAdapter.isEdit) {
            b.edit.callOnClick()
        }

        dashboardTilesAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (dashboardTilesAdapter.isEdit) {
            b.edit.callOnClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {

        dashboard.tiles = dashboardTilesAdapter.tiles.toList()

        super.onPause()
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = settings.spanCount

        dashboardTilesAdapter = TilesAdapter(this, spanCount)
        dashboardTilesAdapter.setHasStableIds(true)

        b.recyclerView.adapter = dashboardTilesAdapter
        b.recyclerView.setItemViewCacheSize(20)

        val layoutManager = TileGridLayoutManager(this, spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val t = dashboardTilesAdapter.tiles[position]
                return when {
                    t.height != 1 || t.width > spanCount -> spanCount
                    else -> t.width
                }
            }
        }

        b.recyclerView.layoutManager = layoutManager

        dashboardTilesAdapter.submitList(dashboard.tiles.toMutableList())

        if (dashboardTilesAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        dashboardTilesAdapter.isEdit = !dashboardTilesAdapter.isEdit

        if (dashboardTilesAdapter.isEdit) {

            b.remove.visibility = View.VISIBLE
            b.add.visibility = View.VISIBLE
            b.duo.setBackgroundResource(R.drawable.button_swap)
        } else {

            b.remove.visibility = View.GONE
            b.add.visibility = View.GONE
            b.duo.setBackgroundResource(R.drawable.button_more)
        }

        b.ban.text = if (dashboardTilesAdapter.isEdit) {
            getString(R.string.swap_mode)
        } else {
            settings.dashboardTagName.uppercase(Locale.getDefault())
        }

        if (!dashboardTilesAdapter.isEdit) {
            b.recyclerView.suppressLayout(false)
            dashboard.tiles = dashboardTilesAdapter.tiles.toList()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setOnClick() {
        if (!dashboardTilesAdapter.isEdit) {
            Intent(this, DashboardSettingsActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)

                finish()
                startActivity(it)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (dashboardTilesAdapter.isEdit) {

            dashboardTilesAdapter.swapMode = true

            b.ban.text = getString(R.string.swap_mode)

            for (t in dashboardTilesAdapter.tiles) {
                t.isEdit = true
                t.flag(false)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun removeOnClick() {
        if (dashboardTilesAdapter.isEdit && !dashboardTilesAdapter.removeMode) {

            dashboardTilesAdapter.removeMode = true

            b.ban.text = getString(R.string.remove_mode)

            for (t in dashboardTilesAdapter.tiles) {
                t.flag(false)
            }

            createToast(this, getString(R.string.dashboard_remove))
        } else if (dashboardTilesAdapter.removeMode) {

            var toDelete = false

            for (t in dashboardTilesAdapter.tiles) {
                if (t.flag) {
                    toDelete = true
                    break
                }
            }

            if (!toDelete) {
                createToast(this, getString(R.string.dashboard_remove), 1)
            } else {

                @SuppressLint("ShowToast")
                val snackbar = Snackbar.make(
                    b.root,
                    getString(R.string.snackbar_confirmation),
                    Snackbar.LENGTH_LONG
                ).margins().setAction("YES") {

                    for ((i, t) in dashboardTilesAdapter.tiles.withIndex()) {


                        if (t.flag) {
                            dashboardTilesAdapter.tiles.removeAt(i)

                            dashboardTilesAdapter.notifyItemRemoved(i)
                            dashboardTilesAdapter.notifyItemRangeChanged(
                                i,
                                dashboardTilesAdapter.itemCount - i
                            )

                            if (dashboardTilesAdapter.itemCount == 0) {
                                b.placeholder.visibility = View.VISIBLE
                            }

                            break
                        }
                    }
                }

                val snackBarView = snackbar.view
                snackBarView.translationY = -20.toPx().toFloat()
                snackbar.show()
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun addOnClick() {
        Intent(this, NewTileActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)

            finish()
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun onServiceReady() {
        for (dg in foregroundService.daemonGroupCollection.daemonsGroups) {
            if (dg.d.name == dashboard.name) {
                dg.mqttd?.data?.observe(this, { p ->
                    if (p.first != null && p.second != null) {
                        foregroundService.alarm.on(1000)
                    }
                })
            }
        }
    }

    //ObjectAnimator.ofFloat(b.indicator, "translationX", distance)
    //.apply {
    //    this.duration = duration
    //    start()
    //}
}