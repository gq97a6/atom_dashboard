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

        suspend fun haltForService() = withTimeoutOrNull(10000) {
            //Wait for service
            while (true) {
                if (service?.isStarted == true) break
                else delay(50)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val intent = Intent(this, ForegroundService::class.java)
        intent.action = "STOP"

        val pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(this, "foreground_service_id").setAutoCancel(false)
                .setOngoing(true).setContentTitle("Server working in background")
                .setSmallIcon(R.mipmap.ic_icon_bold_round).setPriority(PRIORITY_MIN)
                .addAction(R.drawable.ic_trashcan, "stop working in background", pendingIntent)
                .setVisibility(VISIBILITY_SECRET).setSilent(true)

        startForeground(1, notification.build())

        service = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ""

        //Stop service and close the app
        if (action == "STOP") {
            isStarted = false

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

            //finishAffinity()
        } else { //Initialize globals based on action
            //TODO: test if globals persists even if they are initialized outside service
            //initializeGlobals(1)
            isStarted = true
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