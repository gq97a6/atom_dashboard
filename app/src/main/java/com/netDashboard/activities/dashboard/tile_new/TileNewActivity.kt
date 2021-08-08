package com.netDashboard.activities.dashboard.tile_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.tile_properties.TilePropertiesActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityTileNewBinding
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList
import com.netDashboard.tile.TilesAdapter

class TileNewActivity : AppCompatActivity() {
    private lateinit var b: ActivityTileNewBinding

    private var dashboardId: Long = 0
    private lateinit var dashboard: Dashboard
    private lateinit var newTileTilesAdapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityTileNewBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardId = intent.getLongExtra("dashboardId", 0)
        dashboard = Dashboards.get(dashboardId)

        setupRecyclerView()

        newTileTilesAdapter.onItemClick = { tileId ->
            if (tileId >= 0) {
                Intent(this, TilePropertiesActivity::class.java).also {
                    it.putExtra("dashboardId", dashboardId)
                    it.putExtra("tileId", tileAdd(tileId))
                    startActivity(it)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardId", dashboardId)
            startActivity(it)
            finish()
        }
    }

    private fun setupRecyclerView() {
        val spanCount = 3
        newTileTilesAdapter = TilesAdapter(this, spanCount)

        val list = TileTypeList.get()
        newTileTilesAdapter.submitList(list as MutableList<Tile>)

        newTileTilesAdapter.editType.setAdd()
        b.ntRecyclerView.adapter = newTileTilesAdapter

        val layoutManager = GridLayoutManager(this, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return newTileTilesAdapter.list[position].width
            }
        }

        b.ntRecyclerView.layoutManager = layoutManager
    }

    private fun tileAdd(id: Int): Int {

        val tile = TileTypeList.getTileById(id)
        tile.width = 1
        tile.height = 1

        var list = dashboard.tiles

        if (list.isEmpty()) {
            list = mutableListOf(tile)
        } else {
            list.add(tile)
        }
        
        dashboard.tiles = list

        return list.size - 1
    }
}