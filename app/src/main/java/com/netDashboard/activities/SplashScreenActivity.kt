package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.main_settings.MainSettings

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var settings: MainSettings
    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)

        settings = MainSettings(filesDir.canonicalPath).getSaved()

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        //TMP
        val rootPath = "${filesDir.canonicalPath}/dashboard_data"
        FolderTree("$rootPath/test0").check()
        FolderTree("$rootPath/test1").check()
        FolderTree("$rootPath/test2").check()
        FolderTree("$rootPath/test3").check()
        FolderTree("$rootPath/test4").check()
        FolderTree("$rootPath/test5").check()
        //TMP

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                service = s
                onServiceReady()
            }
        })
    }

    private fun onServiceReady() {
        if (settings.lastDashboardName != null) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", settings.lastDashboardName)
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
