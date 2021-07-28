package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.settings.Settings
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.slider.SliderTile

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)

        rootFolder = filesDir.canonicalPath.toString()
        FolderTree.build()

        //TMP
        FolderTree.buildDashboard("example_dashboard")
        FolderTree.buildDashboard("test0")
        FolderTree.buildDashboard("test1")
        FolderTree.buildDashboard("test2")
        //TMP

        Dashboards.getSaved()

        //tmp
        val test:MutableList<Tile> = mutableListOf(
            ButtonTile(),
            ButtonTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            SliderTile(),
            ButtonTile(),
            ButtonTile()
        )

        Dashboards.get("example_dashboard").tiles = test
        //tmp

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                service = s
                onServiceReady()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun onServiceReady() {
        for (d in Dashboards.get()) {
            d.daemonGroup = service.dgc.get(d.name)
        }

        if (Settings.lastDashboardName != null) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", Settings.lastDashboardName)
                overridePendingTransition(0, 0)
                startActivity(it)
                finish()
            }
        } else {

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}
