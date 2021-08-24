package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Theme

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        Theme.apply(this, b.root)
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

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun onServiceReady() {
        ForegroundService.service = service

        for (d in Dashboards.getList()) {
            d.daemonGroup = service.dgc.get(d.name)
        }

        if (Settings.lastDashboardId != null && Settings.startFromLast) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardId", Settings.lastDashboardId)
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
