package com.netDashboard.abyss

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LifecycleService
import com.netDashboard.R
import com.netDashboard.abyss.demons.Mqttd
import com.netDashboard.createToast
import com.netDashboard.main_activity.dashboard_activity.Dashboard
import com.netDashboard.tiles.Tile
import java.io.Serializable

class Abyss : Serializable, LifecycleService() {

    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings
    private lateinit var mqttd: Mqttd
    private lateinit var tiles: List<Tile>

    override fun onCreate() {
        super.onCreate()

        dashboard = Dashboard(filesDir.canonicalPath, "main")
        settings = dashboard.settings

        tiles = dashboard.tiles

        mqttd = Mqttd("tcp://192.168.0.29:1883")
        mqttd.connect(this)

        Thread {
            var connectionDeployed = false

            mqttd.connect(this)

            while (!mqttd.isConnected) {

                if(!connectionDeployed) {

                    connectionDeployed = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        connectionDeployed = false
                        mqttd.connect(this)
                    }, 5000)
                }
            }

            //Connection established

            mqttd.subscribe("abc")

            createToast(this, "done")

        }.start()

        mqttd.data.observe(this, { p ->
            if (p.first != "R73JETTY") {
                for (t in tiles) {
                    t.onData(p.first, p.second)
                }
            }
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notification = NotificationCompat.Builder(this, "foreground_service_id")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Server working in background")
            .setContentText("Running servers: MQTT")
            //.setSubText("SubText")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(PRIORITY_MIN)
            .setVisibility(VISIBILITY_SECRET)
            .setSilent(true)

        startForeground(1, notification.build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dashboard.tiles = tiles

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        dashboard.tiles = tiles
        mqttd.disconnect()

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

fun runAbyss(context: Context) {

    Intent(context, Abyss::class.java).also {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(it)
        } else {
            context.startService(it)
        }
    }
}

fun stopAbyss(context: Context) {

    Intent(context, Abyss::class.java).also {
        context.stopService(it)
    }
}