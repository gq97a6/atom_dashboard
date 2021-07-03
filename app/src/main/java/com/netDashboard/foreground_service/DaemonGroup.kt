 package com.netDashboard.foreground_service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Bluetoothd
import com.netDashboard.foreground_service.demons.Mqttd
import java.io.Serializable

class DaemonGroup(private val context: Context, val dashboard: Dashboard) : Serializable {

    var mqttd = Mqttd(context, dashboard.mqttURI)
    var bluetoothd = Bluetoothd()

    init {
        start()
    }

    private fun start() {
        //MQTT
        if (dashboard.mqttEnabled) {
            mqttd.start()

            mqttd.conHandler.isDone.observe(context as LifecycleOwner, { isDone ->
                if (isDone) {
                    val list: MutableList<String> = mutableListOf()
                    for (tile in dashboard.tiles) {
                        for (topic in tile.mqttTopics.sub.get()) {
                            if (!list.contains(topic)) {
                                mqttd.subscribe(topic)
                                list.add(topic)
                            }
                        }
                    }
                }
            })

            //Bluetooth
            //if (dashboard.properties.bluetoothEnabled) bluetoothd.start()
        }
    }

    fun stop() {
        mqttd.stop()
        //bluetoothd.stop()
    }
}