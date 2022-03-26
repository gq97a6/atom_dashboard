package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.databinding.ActivitySplashScreenBinding
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
import com.alteratom.dashboard.G

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this)

        rootFolder = filesDir.canonicalPath.toString()

        if (service != null) {
            service?.dgManager?.assign()
            onServiceReady()
        } else {
            G.initialize()

            val foregroundServiceHandler = ForegroundServiceHandler(this)
            foregroundServiceHandler.service.observe(this) { s ->
                if (s != null) {
                    service?.finishAffinity = { finishAffinity() }
                    onServiceReady()
                }
            }

            foregroundServiceHandler.start()
            foregroundServiceHandler.bind()
        }
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
        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }, 500)
    }
}