@file:Suppress("unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused", "unused",
    "unused", "unused", "unused", "unused", "unused"
)

package com.netDashboard.abyss

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netDashboard.R
import com.netDashboard.createToast
import java.io.*
import java.net.*

class Abyss : Serializable, Service() {

    val udpd = Udpd(this, 65535)
    //private lateinit var abyssFileName: String

    override fun onCreate() {
        Log.i("OUY", "onCreate")

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
        Log.i("OUY", "onStartCommand")

        if(!udpd.running) udpd.start()

        return START_STICKY
    }

    override fun onDestroy() {
        Log.i("OUY", "onDestroy")

        udpd.kill()
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
    Log.i("OUY", "runAbyss")

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

class Udpd(val context: Context, private var port: Int) : Thread() {

    private lateinit var socket: DatagramSocket

    var running = false

    private var counter = 0

    private val buf = ByteArray(256)
    private var data = MutableLiveData("C9ZF56ZLF4EW5355")

    override fun run() {
        val packet = DatagramPacket(buf, buf.size)

        try {
            socket = DatagramSocket(null)
            socket.reuseAddress = true
            socket.bind(InetSocketAddress(port))
        } catch (e: Exception) {
            Log.d("OUY", e.toString())
        }

        while (running) {
            try {
                socket.receive(packet)

                data.postValue(String(packet.data, 0, packet.length))

                counter++

                createToast(context, counter.toString())

                socket.send(packet)

            } catch (e: Exception) {
                Log.d("OUY", e.toString())
                return
            }
        }

        socket.close()
    }

    override fun start() {
        super.start()

        running = true
    }

    fun kill() {
        running = false
    }

    fun receive(): LiveData<String> {
        return data
    }


    fun send(data: String, address: String, port: Int) {
        //Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val inetAddress = InetAddress.getByName(address)
            val packet =
                DatagramPacket(data.toByteArray(), data.toByteArray().size, inetAddress, port)

            if (packet.port == this.port) {
                this.socket.send(packet)
            } else {
                val socket = DatagramSocket(port)
                socket.send(packet)
                socket.close()
            }
        } catch (e: BindException) {
            Log.e("OUY", e.toString())
        }
    }
}

//class tcpd(private val port: Int):Thread() {
//TODO
//serverSocket = ServerSocket()
//serverSocket.setReuseAddress(true)
//serverSocket.bind(InetSocketAddress(SERVER_PORT))
//}

//class mqttd(private val port: Int):Thread() {
//TODO
//}