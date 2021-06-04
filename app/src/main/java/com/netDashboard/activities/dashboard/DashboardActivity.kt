package com.netDashboard.activities.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.abyss.Abyss
import com.netDashboard.activities.dashboard.new_tile.NewTileActivity
import com.netDashboard.activities.dashboard.settings.DashboardSettingsActivity
import com.netDashboard.createToast
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.DashboardActivityBinding
import com.netDashboard.foreground_service.saveForegroundAbyss
import com.netDashboard.foreground_service.stopForegroundAbyss
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

    private lateinit var abyss: Abyss

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        saveForegroundAbyss(this, dashboardName)
        abyss = Abyss(this, filesDir.canonicalPath, dashboardName, false)

        Thread {

            //Wait for connection
            while (!abyss.mqttd.isConnected) {
                continue
            }

            //Connection established
            stopForegroundAbyss(this)
            createToast(this, "done")

        }.start()

        setupRecyclerView()

        //Set dashboard tag name
        b.ban.text = settings.dashboardTagName.uppercase(Locale.getDefault())

        for (i in 0 until dashboardTilesAdapter.itemCount) {
            dashboardTilesAdapter.tiles[i].mqttd = abyss.mqttd
        }

        b.edit.setOnClickListener {
            editOnClick()
        }

        b.set.setOnClickListener {
            setOnClick()
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

        if (dashboardTilesAdapter.swapMode || dashboardTilesAdapter.removeMode) {
            b.edit.callOnClick()
        }
    }

    override fun onBackPressed() {
        if (dashboardTilesAdapter.swapMode || dashboardTilesAdapter.removeMode) {
            b.edit.callOnClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {

        dashboard.tiles = dashboardTilesAdapter.tiles.toList()

        abyss.save()

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
                return if (dashboardTilesAdapter.tiles[position].height == 1) {
                    dashboardTilesAdapter.tiles[position].width
                } else {
                    spanCount
                }
            }
        }

        abyss.mqttd.data.observe(this, { p ->

            if (p.first != "R73JETTY") {

                for (i in 0 until dashboardTilesAdapter.itemCount) {
                    dashboardTilesAdapter.tiles[i].onData(p.first, p.second)
                }
            }
        })

        b.recyclerView.layoutManager = layoutManager
        //b.recyclerView.itemAnimator?.changeDuration = 0

        dashboardTilesAdapter.submitList(dashboard.tiles.toMutableList())

        if (dashboardTilesAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        dashboardTilesAdapter.swapMode =
            !(dashboardTilesAdapter.swapMode || dashboardTilesAdapter.removeMode)

        dashboardTilesAdapter.removeMode = false

        if (dashboardTilesAdapter.swapMode) {
            b.ban.text = getString(R.string.swap_mode)

            b.remove.visibility = View.VISIBLE
            b.add.visibility = View.VISIBLE
            b.set.setBackgroundResource(R.drawable.button_swap)

            dashboardTilesAdapter.swapModeLock = false
        } else {
            b.ban.text = settings.dashboardTagName.uppercase(Locale.getDefault())

            b.remove.visibility = View.GONE
            b.add.visibility = View.GONE

            b.set.setBackgroundResource(R.drawable.button_more)

            dashboard.tiles = dashboardTilesAdapter.tiles.toList()
        }

        for (i in 0 until dashboardTilesAdapter.itemCount) {
            dashboardTilesAdapter.tiles[i].editMode(dashboardTilesAdapter.swapMode)
            dashboardTilesAdapter.tiles[i].flag(false)
        }

        dashboardTilesAdapter.notifyDataSetChanged()
    }

    //----------------------------------------------------------------------------------------------

    private fun setOnClick() {
        if (dashboardTilesAdapter.removeMode) {
            dashboardTilesAdapter.removeMode = false
            dashboardTilesAdapter.swapMode = true
            b.ban.text = getString(R.string.swap_mode)

            for (i in 0 until dashboardTilesAdapter.itemCount) {
                dashboardTilesAdapter.tiles[i].editMode(true)
                dashboardTilesAdapter.tiles[i].flag(false)
            }
        } else if (!dashboardTilesAdapter.swapMode) {
            Intent(this, DashboardSettingsActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)

                finish()
                startActivity(it)
            }
        }

    }

    //----------------------------------------------------------------------------------------------

    private fun removeOnClick() {
        if (dashboardTilesAdapter.removeMode) {
            var toDelete = false


            for (i in 0 until dashboardTilesAdapter.itemCount) {
                if (dashboardTilesAdapter.tiles[i].flag()) {
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

                    for (i in 0 until dashboardTilesAdapter.itemCount) {

                        if (dashboardTilesAdapter.tiles[i].flag()) {
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
        } else if (dashboardTilesAdapter.swapMode) {
            dashboardTilesAdapter.swapMode = false
            dashboardTilesAdapter.removeMode = true
            b.ban.text = getString(R.string.remove_mode)

            for (i in 0 until dashboardTilesAdapter.itemCount) {
                dashboardTilesAdapter.tiles[i].flag(false)
            }

            createToast(this, getString(R.string.dashboard_remove))
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


    //ObjectAnimator.ofFloat(b.indicator, "translationX", distance)
    //.apply {
    //    this.duration = duration
    //    start()
    //}
}