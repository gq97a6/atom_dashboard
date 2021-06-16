package com.netDashboard.activities.dashboard.new_tile.config_new_tile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        if (settings.spanCount.toFloat() > 1f) {
            b.cntWidth.valueFrom = 1f
            b.cntWidth.valueTo = settings.spanCount.toFloat()
        } else {
            b.cntWidth.valueFrom = 0f
            b.cntWidth.valueTo = 1f
            b.cntWidth.isEnabled = false
        }

        b.cntTileType.text = tile.name

        b.cntWidth.addOnChangeListener { _, value, _ ->
            b.cntWidthValue.text = value.toInt().toString()
            if (value != settings.spanCount.toFloat()) b.cntHeight.value = 1f
        }

        b.cntHeight.addOnChangeListener { _, value, _ ->
            b.cntHeightValue.text = value.toInt().toString()

            if (settings.spanCount.toFloat() > 1f) {
                if (value != 1f) b.cntWidth.value = settings.spanCount.toFloat()
            }
        }

        b.cntAdd.setOnClickListener {
            configTile(tile)

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", dashboardName)
                finish()
                startActivity(it)
            }
        }
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