package com.netDashboard.abyss

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.core.app.NotificationCompat.VISIBILITY_SECRET
import androidx.core.app.NotificationManagerCompat
import com.netDashboard.R
import com.netDashboard.createToast
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*


class Abyss : Service() {

    var udpd = udpd(this, 4445)

    override fun onCreate() {
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
        udpd.start()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }


    private val binder = AbyssBinder()

    inner class AbyssBinder : Binder() {
        fun getService(): Abyss = this@Abyss
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onDestroy() {
        Log.d("OUY", "END")
        udpd.close()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "foreground_service_id",
            "Server service notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "com/netDashboard/new_tile_activities"
        }

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

class AbyssHandler(var context: Context) {
    var isBounded: Boolean = false
    lateinit var service: Abyss

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, IBinder: IBinder) {
            val binder = IBinder as Abyss.AbyssBinder
            service = binder.getService()
            isBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBounded = false
        }
    }

    fun start() {
        Intent(context, Abyss::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(it)
            } else {
                context.startService(it)
            }
        }
    }

    fun stop() {
        Intent(context, Abyss::class.java).also {
            context.stopService(it)
        }
    }

    fun bind() {
        Intent(context, Abyss::class.java).also {
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

class udpd(private val context: Context, private val port: Int) : Thread() {
    private lateinit var socket: DatagramSocket

    init {
        try {
            socket = DatagramSocket(port)

        } catch (e: Exception) {
            Log.d("OUY", "PORT_ERR")
        }
    }

    private var running = false
    private val buf = ByteArray(256)
    private var data = ""

    override fun run() {
        val packet = DatagramPacket(buf, buf.size)
        running = true

        while (running) {
            try {
                socket.receive(packet)


                data = String(packet.data, 0, packet.length)
                onData(data)
                socket.send(packet)

            } catch (e: Exception) {
                Log.d("OUY", "EXCEPTION")
                return
            }
        }

        socket.close()
    }

    private fun onData(data: String) {
        val notification = NotificationCompat.Builder(context, "foreground_service_id")
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentTitle("Boop!")
            .setContentText("You have been booped!")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(2)

        with(NotificationManagerCompat.from(context)) {
            notify(2, notification.build())
        }

        createToast(context, data)
    }

    fun getData(): String {
        return data
    }

    fun sendCmd(cmd: String, adr: String = "192.168.0.18", port: Int = 4445) {
        //Hack Prevent crash (sending should be done using an async task)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        try {
            val address = InetAddress.getByName(adr)
            val packet = DatagramPacket(cmd.toByteArray(), cmd.toByteArray().size, address, port)

            if (packet.port == this.port) {
                this.socket.send(packet)
            } else {
                val socket = DatagramSocket(port)
                socket.send(packet)
                socket.close()
            }
        } catch (e: BindException) {
            Log.e("ERR", "IOException: " + e.message)
        }
    }

    fun close() {
        socket.close()
    }
}

//class tcpd(private val port: Int):Thread() {
//TODO
//}

//class mqttd(private val port: Int):Thread() {
//TODO
//}