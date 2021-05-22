package com.netDashboard.abyss

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import com.netDashboard.R
import com.netDashboard.dashboard_activity.Dashboard
import java.io.Serializable

class Abyss : Serializable, Service() {

    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    override fun onCreate() {

        dashboard = Dashboard(filesDir.canonicalPath, "main")
        settings = dashboard.settings

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notification = NotificationCompat.Builder(this, "foreground_service_id")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentTitle("Server working in background")
            .setContentText("Running servers: UDP")
            //.setSubText("SubText")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(PRIORITY_MIN)
            .setVisibility(VISIBILITY_SECRET)
            .setNotificationSilent()

        startForeground(1, notification.build())
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
    }

    override fun onBind(intent: Intent): IBinder? {
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

//class btd(private val port: Int):Thread() {
//TODO
//}

//class mqttd(private val port: Int):Thread() {
//TODO
//}

//class Udpd(val context: Context, private var port: Int, dashboard: Dashboard) :
//    Thread() {
//
//    var running = false
//
//    private  var socket: DatagramSocket
//    val tiles = dashboard.tiles
//
//    private val buf = ByteArray(5000)
//    private var data = MutableLiveData("C9ZF56ZLF4EW5355")
//
//    override fun run() {
//        val packet = DatagramPacket(buf, buf.size)
//
//        try {
//            socket = DatagramSocket(null)
//            socket.reuseAddress = true
//            socket.bind(InetSocketAddress(port))
//        } catch (e: Exception) {
//            Log.d("OUY", e.toString())
//        }
//
//        val threadsMaxNum = 10
//
//        val threadsNum: Int
//        val tilesPerThread: Int
//
//        when {
//            threadsMaxNum < tiles.size -> {
//
//                threadsNum = threadsMaxNum
//
//                tilesPerThread = if (tiles.size % threadsMaxNum != 0) {
//                    (tiles.size - (tiles.size % threadsMaxNum)) / threadsNum
//                } else {
//                    tiles.size / threadsNum
//                }
//            }
//
//            else -> {
//                threadsNum = tiles.size
//                tilesPerThread = 1
//            }
//        }
//
//        while (running) {
//            try {
//                socket.receive(packet)
//
//                for (t in 1 until threadsNum) {
//
//                    Thread {
//                        for (i in tilesPerThread * (t - 1) until tilesPerThread * t) {
//                            tiles[i].onData("1", false)
//                        }
//                    }.start()
//                }
//
//                Thread {
//                    for (i in tilesPerThread * (threadsNum - 1) until tiles.size) {
//                        tiles[i].onData("1", false)
//                    }
//                }.start()
//
//                //socket.send(packet)
//
//                val threadSet: Set<Thread> = getAllStackTraces().keys
//                Log.i("OUY", threadSet.size.toString())
//            } catch (e: Exception) {
//                Log.d("OUY", e.toString())
//                return
//            }
//        }
//
//        socket.close()
//    }
//
//    override fun start() {
//        super.start()
//
//        running = true
//    }
//
//    fun kill() {
//        running = false
//    }
//
//    fun receive(): LiveData<String> {
//        return data
//    }
//
//
//    fun send(data: String, address: String, port: Int) {
//        //Hack Prevent crash (sending should be done using an async task)
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        try {
//            val inetAddress = InetAddress.getByName(address)
//            val packet =
//                DatagramPacket(data.toByteArray(), data.toByteArray().size, inetAddress, port)
//
//            if (packet.port == this.port) {
//                this.socket.send(packet)
//            } else {
//                val socket = DatagramSocket(port)
//                socket.send(packet)
//                socket.close()
//            }
//        } catch (e: BindException) {
//            Log.e("OUY", e.toString())
//        }
//    }
//}