package com.netDashboard.main_activity.dashboard_activity.new_tile_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.main_activity.dashboard_activity.new_tile_activity.config_new_tile_activity.ConfigNewTileActivity
import com.netDashboard.main_activity.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.NewTileActivityBinding
import com.netDashboard.tiles.TilesAdapter
import com.netDashboard.tiles.TilesList
import com.netDashboard.tiles.Tile

class NewTileActivity : AppCompatActivity() {
    private lateinit var b: NewTileActivityBinding

    private lateinit var dashboardName: String
    private lateinit var newTileTilesAdapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = NewTileActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""

        setupRecyclerView()

        newTileTilesAdapter.getTileOnClickLiveData().observe(this, { tileId ->
            if (tileId >= 0) {
                Intent(this, ConfigNewTileActivity::class.java).also {
                    it.putExtra("dashboardName", dashboardName)
                    it.putExtra("tileId", tileId)

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
        b.recyclerView.adapter = newTileTilesAdapter

        val layoutManager = GridLayoutManager(this, spanCount)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return newTileTilesAdapter.tiles[position].width
            }
        }

        val list = TilesList().get()
        for ((i, _) in list.withIndex()) {
            list[i].editMode(true)
        }

        b.recyclerView.layoutManager = layoutManager
        newTileTilesAdapter.submitList(list as MutableList<Tile>)
    }
}