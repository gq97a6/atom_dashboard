package com.netDashboard.config_new_tile_activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.netDashboard.dashboard_activity.DashboardActivity
import com.netDashboard.dashboard_settings_activity.DashboardSettings
import com.netDashboard.databinding.ConfigNewTileActivityBinding
import com.netDashboard.main_activity.MainActivity
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.Tiles

class ConfigNewTileActivity : AppCompatActivity() {
    private lateinit var b: ConfigNewTileActivityBinding
    private lateinit var settings: DashboardSettings

    private lateinit var dashboardName: String
    private lateinit var dashboardFileName: String
    private lateinit var dashboardSettingsFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ConfigNewTileActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val tileId = intent.getIntExtra("tileId", 0)
        val tile = Tiles().getTileList()[tileId]

        dashboardFileName = intent.getStringExtra("dashboardFileName") ?: ""
        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboardSettingsFileName = intent.getStringExtra("dashboardSettingsFileName") ?: ""

        if (dashboardName.isEmpty() || dashboardFileName.isEmpty() || dashboardSettingsFileName.isEmpty()) {
            Intent(this, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }

        dashboardFileName = filesDir.canonicalPath + "/" + dashboardName
        dashboardSettingsFileName = filesDir.canonicalPath + "/settings_" + dashboardName
        settings = DashboardSettings().getSettings(dashboardSettingsFileName)

        b.height.value = 1f
        b.height.valueFrom = 1f
        b.height.valueTo = 10f

        b.width.value = 1f

        if(settings.spanCount.toFloat() > 1f) {
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
                if(settings.spanCount.toFloat() > 1f) {
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

    private fun configTile(tile: Tile) {
        tile.width = b.width.value.toInt()
        tile.height = b.height.value.toInt()

        var list = Tiles().getList(dashboardFileName)

        if (list.isEmpty()) {
            list = listOf(tile)
        } else {
            list = list.toMutableList()
            list.add(tile)
            list = list.toList()
        }

        Tiles().saveList(list, dashboardFileName)
    }
}