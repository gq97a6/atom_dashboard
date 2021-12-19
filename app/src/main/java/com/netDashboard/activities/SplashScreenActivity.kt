package com.netDashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.globals.G

@SuppressLint("CustomSplashScreen")
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
        service.finishFlag.observe(this) { flag ->
            if (flag) finishAffinity()
        }

        ForegroundService.service = service
        service.dgManager.assign()

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }

            //if (settings.startFromLast) {
            //    settings.lastDashboardId?.let {
            //        setCurrentDashboard(it)
            //        Intent(this, DashboardActivity::class.java).also {
            //            overridePendingTransition(0, 0)
            //            startActivity(it)
            //            finish()
            //        }
            //    }
            //}
        }, 500)
    }
}
