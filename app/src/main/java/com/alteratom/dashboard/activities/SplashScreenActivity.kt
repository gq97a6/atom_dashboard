package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.get
import com.alteratom.R
import com.alteratom.dashboard.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
import com.alteratom.dashboard.jiggle
import com.alteratom.databinding.ActivitySplashScreenBinding

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this)

        //if (Build.VERSION.SDK_INT > 30) b.ssBox.visibility = GONE

        b.ssIcon.setBackgroundResource(if (G.theme.a.isDark) R.drawable.ic_icon_light  else R.drawable.ic_icon)

        rootFolder = filesDir.canonicalPath.toString()

        if (service != null && dashboards.isNotEmpty()) {
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
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            overridePendingTransition(0, 0)
            finish()
        }
    }
}