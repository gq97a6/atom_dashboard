package com.netDashboard.main_activity.dashboard_activity.new_tile_activity.config_new_tile_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.netDashboard.main_activity.dashboard_activity.Dashboard
import com.netDashboard.main_activity.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.ConfigNewTileActivityBinding
import com.netDashboard.tiles.TilesList
import com.netDashboard.tiles.Tile

class ConfigNewTileActivity : AppCompatActivity() {
    private lateinit var b: ConfigNewTileActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ConfigNewTileActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val tileId = intent.getIntExtra("tileId", 0)
        val tile = TilesList().get()[tileId]

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        b.height.value = 1f
        b.height.valueFrom = 1f
        b.height.valueTo = 10f

        b.width.value = 1f

        if (settings.spanCount.toFloat() > 1f) {
            b.width.valueFrom = 1f
            b.width.valueTo = settings.spanCount.toFloat()
        } else {
            b.width.valueFrom = 0f
            b.width.valueTo = 1f
            b.width.isEnabled = false
        }

        b.tileType.text = tile.name

        b.push.setOnClickListener {
            configTile(tile)

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)
                finish()
                startActivity(it)
            }
        }

        b.height.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (settings.spanCount.toFloat() > 1f) {
                    if (b.height.value == 1f) {
                        b.width.isEnabled = true
                        b.warning.visibility = View.GONE
                    } else {
                        b.warning.visibility = View.VISIBLE
                        b.width.isEnabled = false
                        b.width.value = settings.spanCount.toFloat()
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            finish()
            startActivity(it)
        }
    }

    private fun configTile(tile: Tile) {
        tile.width = b.width.value.toInt()
        tile.height = b.height.value.toInt()

        var list = dashboard.tiles

        if (list.isEmpty()) {
            list = listOf(tile)
        } else {
            list = list.toMutableList()
            list.add(tile)
            list = list.toList()
        }

        dashboard.tiles = list
    }
}