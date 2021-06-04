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
import com.netDashboard.main.Dashboards
import java.io.Serializable

class ForegroundService : Serializable, LifecycleService() {

    private var isRunning = false

    private lateinit var dashboards: MutableList<Dashboard>

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

        val dashboardName = intent?.getStringExtra("dashboardName") ?: ""
        val ifSave = intent?.getBooleanExtra("ifSave", false) ?: false

        when {
            ifSave -> {
                if (isRunning) abyss.save()
            }

            !isRunning -> {
                val dashboards = Dashboards().get(filesDir.canonicalPath)

                for(d in dashboards) {
                    if(d.name == dashboardName) continue
                    abyss = Abyss(this, filesDir.canonicalPath, d.name, true)
                }

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

fun startForegroundAbyss(context: Context, dashboardName: String = "") {

    Intent(context, ForegroundService::class.java).also {

        it.putExtra("dashboardName", dashboardName)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(it)
        } else {
            context.startService(it)
        }
    }
}

fun stopForegroundAbyss(context: Context) {

    Intent(context, ForegroundService::class.java).also {
        context.stopService(it)
    }
}

fun saveForegroundAbyss(context: Context, dashboardName: String = "") {

    Intent(context, ForegroundService::class.java).also {

        it.putExtra("dashboardName", dashboardName)
        it.putExtra("ifSave", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(it)
        } else {
            context.startService(it)
        }
    }
}