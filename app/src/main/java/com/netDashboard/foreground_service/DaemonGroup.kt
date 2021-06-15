package com.netDashboard.foreground_service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Bluetoothd
import com.netDashboard.foreground_service.demons.Mqttd

class DaemonGroup(private val context: Context, rootPath: String, val name: String) {

    val d = Dashboard(rootPath, name)

    var mqttd: Mqttd? = null
    var mqttdSubTopics: MutableList<String> = mutableListOf()

    var bluetoothd: Bluetoothd? = null

    init {
        start()
    }

    private fun start() {

        if (d.settings.mqttEnabled) {
            mqttd = Mqttd(context, d.settings.mqttURI)

            for (t in d.tiles) {
                for (st in t.mqttSubTopics) {
                    if (!mqttdSubTopics.contains(st)) mqttdSubTopics.add(st)
                }
            }

            mqttd?.onConnect?.observe(context as LifecycleOwner, { isConnected ->
                if (isConnected) {
                    for (st in mqttdSubTopics) mqttd?.subscribe(st)
                }
            })

            //mqttd?.data?.observe(context as LifecycleOwner, { p ->
            //    if (p.first != null && p.second != null) {
            //
            //    }
            //})
        }

        if (d.settings.bluetoothEnabled) bluetoothd = Bluetoothd()
    }

    fun stop() {
        mqttd?.stop()
        mqttdSubTopics.clear()
    }

    fun rerun() {
        stop()
        start()
    }
}