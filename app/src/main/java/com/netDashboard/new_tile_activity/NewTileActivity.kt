package com.netDashboard.new_tile_activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.config_new_tile_activity.ConfigNewTileActivity
import com.netDashboard.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.NewTileActivityBinding
import com.netDashboard.main_activity.MainActivity
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter
import com.netDashboard.tiles.Tiles

class NewTileActivity : AppCompatActivity() {
    private lateinit var b: NewTileActivityBinding
    lateinit var newTileTilesAdapter: TilesAdapter

    private lateinit var dashboardName: String
    private lateinit var dashboardFileName: String
    private lateinit var dashboardSettingsFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = NewTileActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardFileName = intent.getStringExtra("dashboardFileName") ?: ""
        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboardSettingsFileName = intent.getStringExtra("dashboardSettingsFileName") ?: ""

        if (dashboardName.isEmpty() || dashboardFileName.isEmpty() || dashboardSettingsFileName.isEmpty()) {
            Intent(this, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }

        setupRecyclerView()

        newTileTilesAdapter.getTileOnClickLiveData().observe(this, { tileId ->
            if (tileId >= 0) {
                Intent(this, ConfigNewTileActivity::class.java).also {
                    it.putExtra("dashboardName", dashboardName)
                    it.putExtra("dashboardFileName", dashboardFileName)
                    it.putExtra("dashboardSettingsFileName", dashboardSettingsFileName)
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

        //Enable edit mode
        val list = Tiles().getTileList()
        for ((i, _) in list.withIndex()) {
            list[i].editMode(true)
        }

        b.recyclerView.layoutManager = layoutManager
        newTileTilesAdapter.submitList(list as MutableList<Tile>)
    }
}