package com.netDashboard.foreground_service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.netDashboard.R
import com.netDashboard.dashboard.Dashboards
import kotlin.system.exitProcess


class ForegroundService : LifecycleService() {
    private var isRunning = false
    lateinit var dgc: DaemonGroups

    companion object {
        var service: ForegroundService? = null
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val intent = Intent(this, ForegroundService::class.java)
        intent.action = "STOP"

        val pendingIntent = PendingIntent
            .getService(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(this, "foreground_service_id")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Server working in background")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(PRIORITY_MIN)
            .addAction(R.drawable.icon_remove_flag, "stop working in background", pendingIntent)
            .setVisibility(VISIBILITY_SECRET)
            .setSilent(true)

        startForeground(1, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            isRunning = false
            Dashboards.save()
            stopForeground(true)
            stopSelf()
            exitProcess(0)
        } else {
            if (!isRunning) {
                dgc = DaemonGroups(this)
                isRunning = true
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        //createNotification(this, "foregroundService", "onDestroy")
        Dashboards.save()

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
            description = "com/netDashboard/notification_channel"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

class ForegroundServiceHandler(var context: Context) {

    var isBounded: Boolean = false
    var service: MutableLiveData<ForegroundService?> = MutableLiveData(null)

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, IBinder: IBinder) {
            val binder = IBinder as ForegroundService.ForegroundServiceBinder
            service.postValue(binder.getService())
            isBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBounded = false
        }
    }

    fun start() {
        Intent(context, ForegroundService::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
            } else {
                context.startService(it)
            }
        }
    }

    fun stop() {
        Intent(context, ForegroundService::class.java).also {
            context.stopService(it)
        }
    }

    fun bind() {
        Intent(context, ForegroundService::class.java).also {
            context.bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (isBounded) {
            context.unbindService(connection)
            isBounded = false
        }
    }
}