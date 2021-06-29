package com.netDashboard.foreground_service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Bluetoothd
import com.netDashboard.foreground_service.demons.Mqttd
import java.io.Serializable

class DaemonGroup(private val context: Context, rootPath: String, val name: String) : Serializable {

    val dashboard = Dashboard(rootPath, name)

    var mqttd: Mqttd? = null
    var bluetoothd: Bluetoothd? = null

    init {
        start()
    }

    private fun start() {
        //MQTT
        if (dashboard.properties.mqttEnabled) {
            mqttd = Mqttd(context, dashboard.properties.mqttURI)

            //mqttd?.onConnectFlag?.observe(context as LifecycleOwner, { isConnected ->
            //    if (isConnected) {
            //        for (st in mqttdSubTopics) mqttd?.subscribe(st)
            //    }
            //})
        }

        //Bluetooth
        if (dashboard.properties.bluetoothEnabled) bluetoothd = Bluetoothd()
    }

    fun stop() {
        mqttd?.stop()
        //bluetoothd?.stop()
    }
}