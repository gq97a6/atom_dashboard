package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowInsetsControllerCompat
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.Theme.Companion.artist
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
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

        b.ssIcon.setBackgroundResource(if (G.theme.a.isDark) R.drawable.ic_icon_light else R.drawable.ic_icon)

        rootFolder = filesDir.canonicalPath.toString()

        if (service != null && dashboards.isNotEmpty()) {
            onServiceReady()
        } else {
            G.initialize()

            val foregroundServiceHandler = ForegroundServiceHandler(this)
            foregroundServiceHandler.service.observe(this) { s ->
                run {}
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