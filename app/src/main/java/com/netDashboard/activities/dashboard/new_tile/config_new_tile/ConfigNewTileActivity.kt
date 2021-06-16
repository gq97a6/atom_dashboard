package com.netDashboard.activities.dashboard.new_tile.config_new_tile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.ActivityConfigNewTileBinding
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList

class ConfigNewTileActivity : AppCompatActivity() {
    private lateinit var b: ActivityConfigNewTileBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityConfigNewTileBinding.inflate(layoutInflater)
        setContentView(b.root)

        val tileId = intent.getIntExtra("tileId", 0)
        val tile = TileTypeList().get()[tileId]

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        b.cntHeight.value = 1f
        b.cntHeight.valueFrom = 1f
        b.cntHeight.valueTo = 10f

        b.cntWidth.value = 1f

        if (settings.spanCount.toFloat() > 1f) {
            b.cntWidth.valueFrom = 1f
            b.cntWidth.valueTo = settings.spanCount.toFloat()
        } else {
            b.cntWidth.valueFrom = 0f
            b.cntWidth.valueTo = 1f
            b.cntWidth.isEnabled = false
        }

        b.cntTileType.text = tile.name

        b.cntAdd.setOnClickListener {
            configTile(tile)

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)
                finish()
                startActivity(it)
            }
        }

        b.cntHeight.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (settings.spanCount.toFloat() > 1f) {
                    if (b.cntHeight.value == 1f) {
                        b.cntWidth.isEnabled = true
                        b.cntWarning.visibility = View.GONE
                    } else {
                        b.cntWarning.visibility = View.VISIBLE
                        b.cntWidth.isEnabled = false
                        b.cntWidth.value = settings.spanCount.toFloat()
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
        tile.width = b.cntWidth.value.toInt()
        tile.height = b.cntHeight.value.toInt()

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