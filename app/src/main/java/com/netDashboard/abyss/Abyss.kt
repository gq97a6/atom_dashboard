package com.netDashboard.abyss

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.abyss.demons.Mqttd
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.tile.Tile

class Abyss(
    context: Context,
    rootPath: String,
    dashboardName: String,
    private val isLive: Boolean
) {

    val dashboard = Dashboard(rootPath, dashboardName)
    private val settings = dashboard.settings

    private lateinit var tiles: List<Tile>

    var mqttd = Mqttd(settings.mqttURI)
    //private var bluetoothd = Bluetoothd()

    init {

        Thread {
            var connectionDeployed = false

            mqttd.connect(context)

            //Wait for connection
            while (!mqttd.client.isConnected) {

                if (!connectionDeployed) {

                    connectionDeployed = true

                    //Try to connect every 5 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        connectionDeployed = false
                        mqttd.connect(context)
                    }, 5000)
                }
            }

            //Connection established
            mqttd.subscribe("abc")

        }.start()

        if (!isLive) {
            tiles = dashboard.tiles

            mqttd.data.observe(context as LifecycleOwner, { p ->
                if (p.first != "R73JETTY") {
                    for (element in tiles) {
                        element.onData(p.first, p.second)
                    }
                }
            })
        }
    }

    fun close() {
        Log.i("OUY", "SAVE")
        if (!isLive) dashboard.tiles = tiles
    }
}