package com.alteratom.dashboard

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.objects.ActivityHandler
import com.alteratom.dashboard.objects.G.initializeGlobals
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ForegroundService : LifecycleService() {

    var finishAffinity: () -> Unit = {}
    var isStarted = false

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
        Logger.log("Service onStartCommand: $action")

        //Stop service and close the app
        if (action == "STOP") {
            isStarted = false
            //TODO: test if globals persists even if they are not used in service
            //DaemonsManager.notifyAllRemoved()

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()

            finishAffinity()
        } else { //Initialize globals based on action
            //TODO: test if globals persists even if they are initialized outside service
            //initializeGlobals(1)
            isStarted = true
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Logger.log("Service destroyed")
        //TODO: test if globals persists even if they are not used in service
        //ActivityHandler.onDestroy()
        //DaemonsManager.notifyAllRemoved()
        super.onDestroy()
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