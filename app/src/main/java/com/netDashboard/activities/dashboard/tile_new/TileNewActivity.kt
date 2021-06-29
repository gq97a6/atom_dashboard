package com.netDashboard.activities.dashboard.tile_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.tile_properties.TilePropertiesActivity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.ActivityNewTileBinding
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList
import com.netDashboard.tile.TilesAdapter

class TileNewActivity : AppCompatActivity() {
    private lateinit var b: ActivityNewTileBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var newTileTilesAdapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityNewTileBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)

        setupRecyclerView()

        newTileTilesAdapter.getTileOnClickLiveData().observe(this, { tileId ->
            if (tileId >= 0) {
                Intent(this, TilePropertiesActivity::class.java).also {
                    it.putExtra("dashboardName", dashboardName)
                    it.putExtra("tileId", tileAdd(tileId))

                    finish()
                    startActivity(it)
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

    private fun setupRecyclerView() {
        val spanCount = 3
        newTileTilesAdapter = TilesAdapter(this, spanCount, "add")
        b.ntRecyclerView.adapter = newTileTilesAdapter

        val layoutManager = GridLayoutManager(this, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return newTileTilesAdapter.tiles[position].p.width
            }
        }

        val list = TileTypeList().get()
        for ((i, _) in list.withIndex()) {
            list[i].isEdit = true
        }

        b.ntRecyclerView.layoutManager = layoutManager
        newTileTilesAdapter.submitList(list as MutableList<Tile>)
    }

    private fun tileAdd(id: Int): Int {

        val tile = TileTypeList().getById(id)
        tile.p.width = 1
        tile.p.height = 1

        var list = dashboard.tiles

        if (list.isEmpty()) {
            list = listOf(tile)
        } else {
            list = list.toMutableList()
            list.add(tile)
        }

        dashboard.tiles = list

        return list.size - 1
    }
}