package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rootFolder = filesDir.canonicalPath.toString()
        G.initialize()

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)

        setContentView(b.root)

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

    override fun onPause() {
        super.onPause()

        AppOn.pause()
        finish()
    }

    private fun onServiceReady() {
        ForegroundService.service = service

        service.finishFlag.observe(this) { flag ->
            if (flag) finishAffinity()
        }

        for (d in dashboards) {
            d.daemonGroup = service.dgc.get(d.name)
        }

        Handler(Looper.getMainLooper()).postDelayed({
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
        }, 500)
    }
}
