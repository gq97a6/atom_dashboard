package com.netDashboard.abyss

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.abyss.demons.Mqttd
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.tile.Tile

class Abyss(
    context: Context,
    rootPath: String,
    dashboardName: String,
    private val isForeground: Boolean
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

                    //Try to connect every 5 seconds
                    Handler(Looper.getMainLooper()).postDelayed({
                        connectionDeployed = false
                        mqttd.connect(context)
                    }, 5000)

                    connectionDeployed = true
                }
            }

            //Connection established
            mqttd.subscribe("abc")

        }.start()

        if (isForeground) {
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

    fun save() {
        if (isForeground) dashboard.tiles = tiles
    }

    fun close() {
        mqttd.disconnect()
    }
}