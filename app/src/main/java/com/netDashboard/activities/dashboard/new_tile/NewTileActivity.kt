package com.netDashboard.activities.dashboard.new_tile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.config_new_tile.ConfigTileActivity
import com.netDashboard.databinding.ActivityNewTileBinding
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList
import com.netDashboard.tile.TilesAdapter

class NewTileActivity : AppCompatActivity() {
    private lateinit var b: ActivityNewTileBinding

    private lateinit var dashboardName: String
    private lateinit var newTileTilesAdapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityNewTileBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""

        setupRecyclerView()

        newTileTilesAdapter.getTileOnClickLiveData().observe(this, { tileId ->
            if (tileId >= 0) {
                Intent(this, ConfigTileActivity::class.java).also {
                    it.putExtra("dashboardName", dashboardName)
                    it.putExtra("tileId", tileId)
                    it.putExtra("newTile", true)

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
                return newTileTilesAdapter.tiles[position].width
            }
        }

        val list = TileTypeList().get()
        for ((i, _) in list.withIndex()) {
            list[i].isEdit = true
        }

        b.ntRecyclerView.layoutManager = layoutManager
        newTileTilesAdapter.submitList(list as MutableList<Tile>)
    }
}