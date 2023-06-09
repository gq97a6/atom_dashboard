package com.alteratom.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import com.alteratom.R
import com.alteratom.dashboard.activities.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull


class ForegroundService : LifecycleService() {

    var finishAffinity: () -> Unit = {}
    var isStarted = false

    companion object {
        var service: ForegroundService? = null

        //Stop service and activity
        fun shut(activity: MainActivity) {
            Intent(activity, ForegroundService::class.java).also {
                it.action = "SHUT"
                activity.startForegroundService(it)
            }
        }

        //Stop service but not the activity
        fun stop(activity: MainActivity) {
            Intent(activity, ForegroundService::class.java).also {
                it.action = "STOP"
                activity.startForegroundService(it)
            }
        }

        fun start(activity: MainActivity) {
            Intent(activity, ForegroundService::class.java).also {
                it.action = "START"
                activity.startForegroundService(it)
            }

        }

        //Wait for service
        suspend fun haltForService() = withTimeoutOrNull(10000) {
            while (true) {
                if (service?.isStarted == true) break
                else delay(100)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val intent = Intent(this, ForegroundService::class.java)
        intent.action = "SHUT"

        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(this, "foreground_service_id").setAutoCancel(false)
                .setContentTitle("Server working in background")
                .setSmallIcon(R.mipmap.ic_icon_bold_round).setPriority(PRIORITY_MIN)
                .addAction(R.drawable.ic_trashcan, "stop working in background", pendingIntent)
                .setVisibility(VISIBILITY_SECRET).setSilent(true)

        startForeground(1, notification.build())

        service = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ""

        if (action == "START") isStarted = true
        else {
            isStarted = false

            if (action == "SHUT") finishAffinity()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

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