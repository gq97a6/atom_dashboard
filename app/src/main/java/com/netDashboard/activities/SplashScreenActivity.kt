package com.netDashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.Activity
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.globals.G
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.globals.G.settings

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

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
        Activity.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Activity.onPause()
        finish()
    }

    private fun onServiceReady() {
        service.finishAffinity = { finishAffinity() }

        ForegroundService.service = service
        service.dgManager.assign()

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }, 500)
    }
}
