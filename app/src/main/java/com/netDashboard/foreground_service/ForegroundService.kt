package com.netDashboard.foreground_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import com.netDashboard.R
import com.netDashboard.abyss.Abyss
import com.netDashboard.dashboard.Dashboard
import java.io.Serializable

class ForegroundService : Serializable, LifecycleService() {

    private var isRunning = false

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    private lateinit var abyss: Abyss

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notification = NotificationCompat.Builder(this, "foreground_service_id")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Server working in background")
            .setContentText("Running servers: MQTT")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(PRIORITY_MIN)
            .setVisibility(VISIBILITY_SECRET)
            .setSilent(true)

        startForeground(1, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        dashboardName = intent?.getStringExtra("dashboardName") ?: ""
        val ifStop = intent?.getBooleanExtra("ifStop", false) ?: false

        when {
            ifStop -> {
                if (isRunning) abyss.close()
                stopBackgroundAbyss(this)
            }

            isRunning -> startBackgroundAbyss(this, dashboardName, true)

            else -> {
                dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
                settings = dashboard.settings

                abyss = Abyss(this, filesDir.canonicalPath, dashboardName, false)

                isRunning = true
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        if (isRunning) abyss.close()

        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "foreground_service_id",
            "Server service notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "com/netDashboard/notification_channel"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun startBackgroundAbyss(context: Context, dashboardName: String, ifStop: Boolean = false) {

    Intent(context, ForegroundService::class.java).also {

        it.putExtra("dashboardName", dashboardName)
        it.putExtra("ifStop", ifStop)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(it)
        } else {
            context.startService(it)
        }
    }
}

fun stopBackgroundAbyss(context: Context) {

    Intent(context, ForegroundService::class.java).also {
        context.stopService(it)
    }
}