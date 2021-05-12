package com.netDashboard.abyss

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.StrictMode
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

class Abyss(val context: Context? = null) : Service() {

    var udpd = udpd(this, 65535) //TODO - context should not be in use

    var isBounded: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, IBinder: IBinder) {
            isBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBounded = false
        }
    }

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
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d("OUY", "END")
    }

    override fun onBind(intent: Intent): IBinder {
        return AbyssBinder()
    }

    inner class AbyssBinder : Binder() {
        //fun getService(): Abyss = this@Abyss
    }

    fun start() {
        Intent(context, Abyss::class.java).also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(it)
            } else {
                context?.startService(it)
            }
        }

        bind()
    }

    fun stop() {
        Intent(context, Abyss::class.java).also {
            context?.stopService(it)
        }

        udpd.kill()
        unbind()
    }

    private fun bind() {
        Intent(context, Abyss::class.java).also {
            context?.bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun unbind() {
        if (isBounded) {
            context?.unbindService(connection)
            isBounded = false
        }
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

class udpd(private val context: Context, var port: Int) : Thread() {

    private lateinit var socket: DatagramSocket

    private var running = false
    private val buf = ByteArray(256)
    private var data = ""

    override fun run() {
        val packet = DatagramPacket(buf, buf.size)

        try {
            socket = DatagramSocket(6667)

        } catch (e: Exception) {
            Log.d("OUY", e.toString())
        }

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

        Log.d("OUY", "CLOSED")

        socket.close()
    }

    private fun onData(data: String) {
        val notification = NotificationCompat.Builder(context, "foreground_service_id")
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentTitle("Title!")
            .setContentText("ContentText")
            .setSmallIcon(R.drawable.icon_main)
            .setPriority(2)

        with(NotificationManagerCompat.from(context)) {
            notify(2, notification.build())
        }

        createToast(context, data)
    }

    fun receive(): String {
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
            Log.e("ERR", "IOException: " + e.message)
        }
    }

    override fun start() {
        super.start()

        running = true
    }

    fun kill() {
        running = false
    }
}

//class tcpd(private val port: Int):Thread() {
//TODO
//}

//class mqttd(private val port: Int):Thread() {
//TODO
//}