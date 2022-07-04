package com.alteratom.dashboard.foreground_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import com.alteratom.R
import com.alteratom.dashboard.ActivityHandler
import com.alteratom.dashboard.foreground_service.demons.DaemonsManager


class ForegroundService : LifecycleService() {

    var finishAffinity: () -> Unit = {}
    var isRunning = false

    companion object {
        var service: ForegroundService? = null
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val intent = Intent(this, ForegroundService::class.java)
        intent.action = "STOP"

        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "foreground_service_id")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Server working in background")
            .setSmallIcon(R.mipmap.ic_icon_bold_round)
            .setPriority(PRIORITY_MIN)
            .addAction(R.drawable.ic_trashcan, "stop working in background", pendingIntent)
            .setVisibility(VISIBILITY_SECRET)
            .setSilent(true)

        startForeground(1, notification.build())

        service = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            isRunning = false
            DaemonsManager.notifyAllDischarged()

            stopForeground(true)
            stopSelf()

            finishAffinity()
        } else if (!isRunning) isRunning = true

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        ActivityHandler.onDestroy()

        if (isRunning) {
            val foregroundServiceHandler = ForegroundServiceHandler(this)
            foregroundServiceHandler.start()
        }

        super.onDestroy()
    }

    private val binder = ForegroundServiceBinder()

    inner class ForegroundServiceBinder : Binder() {
        fun getService(): ForegroundService = this@ForegroundService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "foreground_service_id",
            "Server service notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "com/alteratom/notification_channel"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}