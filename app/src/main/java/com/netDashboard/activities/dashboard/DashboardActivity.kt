package com.netDashboard.activities.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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
import com.netDashboard.tile.TileGridLayoutManager
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toPx
import java.util.*

class DashboardActivity : AppCompatActivity() {
    private lateinit var b: DashboardActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    lateinit var adapter: TilesAdapter

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
        b.tagName.text = settings.dashboardTagName.uppercase(Locale.getDefault())

        b.touch.setOnClickListener {
            touchOnClick()
        }

        b.settings.setOnClickListener {
            settingsOnClick()
        }

        b.edit.setOnClickListener {
            editOnClick()
        }

        b.swap.setOnClickListener {
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

        if (adapter.isEdit) {
            b.edit.callOnClick()
        }

        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (adapter.isEdit) {
            b.touch.callOnClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {

        dashboard.tiles = adapter.tiles.toList()

        super.onPause()
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = settings.spanCount

        adapter = TilesAdapter(this, spanCount, "", dashboardName)
        adapter.setHasStableIds(true)

        b.recyclerView.adapter = adapter
        b.recyclerView.setItemViewCacheSize(20)

        val layoutManager = TileGridLayoutManager(this, spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val t = adapter.tiles[position]
                return when {
                    t.height != 1 || t.width > spanCount -> spanCount
                    else -> t.width
                }
            }
        }

        b.recyclerView.layoutManager = layoutManager

        adapter.submitList(dashboard.tiles.toMutableList())

        if (adapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun touchOnClick() {
        adapter.isEdit = !adapter.isEdit

        if (!adapter.isEdit) {
            b.recyclerView.suppressLayout(false)
            dashboard.tiles = adapter.tiles.toList()
        } else {
            highlightOnly(b.swap)

            adapter.swapMode = true

            for (t in adapter.tiles) {
                t.isEdit = true
                t.flag(false)
            }
        }

        b.bar.visibility = if (adapter.isEdit) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun settingsOnClick() {
        Intent(this, DashboardSettingsActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)

            finish()
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        if (!adapter.isEdit) return

        highlightOnly(b.edit)
        createToast(this, getString(R.string.dashboard_edit), 1)

        adapter.editMode = true
    }

    //----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (!adapter.isEdit) return

        highlightOnly(b.swap)
        createToast(this, getString(R.string.dashboard_swap), 1)

        adapter.swapMode = true
    }

    //----------------------------------------------------------------------------------------------

    private fun removeOnClick() {
        if (!adapter.isEdit) return

        highlightOnly(b.remove)

        if (!adapter.removeMode) {

            adapter.removeMode = true

            createToast(this, getString(R.string.dashboard_remove))
        } else {

            var toDelete = false

            for (t in adapter.tiles) {
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
                ).setAction("YES") {

                    for ((i, t) in adapter.tiles.withIndex()) {


                        if (t.flag) {
                            adapter.tiles.removeAt(i)

                            adapter.notifyItemRemoved(i)
                            adapter.notifyItemRangeChanged(
                                i,
                                adapter.itemCount - i
                            )

                            if (adapter.itemCount == 0) {
                                b.placeholder.visibility = View.VISIBLE
                            }

                            break
                        }
                    }
                }

                val snackBarView = snackbar.view
                snackBarView.translationY = -60.toPx().toFloat()
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

    private fun highlightOnly(button: Button) {
        b.remove.alpha = 0.4f
        b.swap.alpha = 0.4f
        b.edit.alpha = 0.4f
        button.alpha = 1f
    }

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