package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import java.io.File

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //test
        //File("test").writeText("AAAAA")

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)

        setContentView(b.root)

        rootFolder = filesDir.canonicalPath.toString()

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
        AppOn.destroy()
    }

    private fun onServiceReady() {
        ForegroundService.service = service

        for (d in dashboards) {
            d.daemonGroup = service.dgc.get(d.name)
        }

        if (settings.lastDashboardId != null && settings.startFromLast) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardId", settings.lastDashboardId)
                overridePendingTransition(0, 0)
                startActivity(it)
            }
        } else {

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }
}
