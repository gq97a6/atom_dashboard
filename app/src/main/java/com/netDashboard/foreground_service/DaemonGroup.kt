package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Bluetoothd
import com.netDashboard.foreground_service.demons.Mqttd

class DaemonGroup(private val context: Context, rootPath: String, val name: String) {

    val d = Dashboard(rootPath, name)

    var mqttd: Mqttd? = null
    var bluetoothd: Bluetoothd? = null

    init {
        start()
    }

    private fun start() {

        if (d.settings.mqttEnabled) mqttd = Mqttd(context, d.settings.mqttURI)
        if (d.settings.bluetoothEnabled) bluetoothd = Bluetoothd()
    }

    fun stop() {
        mqttd?.stop()
    }

    fun rerun() {
        stop()
        start()
    }
}