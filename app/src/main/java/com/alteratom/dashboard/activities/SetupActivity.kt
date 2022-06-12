package com.alteratom.dashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alteratom.R
import com.alteratom.dashboard.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
import com.alteratom.dashboard.foreground_service.demons.DaemonsManager


@SuppressLint("CustomSplashScreen")
class SetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        val serviceRunning = service?.isRunning == true && dashboards.isNotEmpty()

        rootFolder = filesDir.canonicalPath.toString()
        if (!serviceRunning) G.initialize()

        G.theme.apply(context = this)

        setContent {
            ComposeTheme(Theme.isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column {
                        val icon = if (Theme.isDark) R.drawable.ic_icon_light
                        else R.drawable.ic_icon

                        Image(
                            painterResource(icon), "",
                            modifier = Modifier.size(300.dp),
                            colorFilter = ColorFilter.tint(
                                colors.color.copy(alpha = .4f),
                                BlendMode.SrcAtop
                            )
                        )

                        Spacer(modifier = Modifier.fillMaxHeight(.2f))
                    }
                }
            }
        }

        if (serviceRunning) onServiceReady()
        else {
            val foregroundServiceHandler = ForegroundServiceHandler(this)
            foregroundServiceHandler.service.observe(this) { s ->
                if (s != null) {
                    DaemonsManager.initialize()
                    service?.finishAffinity = { finishAffinity() }
                    onServiceReady()
                }
            }

            foregroundServiceHandler.start()
            foregroundServiceHandler.bind()
        }
    }

    fun onServiceReady() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            overridePendingTransition(0, 0)
            finish()
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
}