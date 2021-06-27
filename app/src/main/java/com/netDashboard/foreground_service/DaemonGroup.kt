package com.netDashboard.foreground_service

import android.content.Context
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Bluetoothd
import com.netDashboard.foreground_service.demons.Mqttd
import java.io.Serializable

class DaemonGroup(private val context: Context, rootPath: String, val name: String) : Serializable {

    val dashboard = Dashboard(rootPath, name)

    var mqttd: Mqttd? = null
    var mqttdSubTopics: MutableList<String> = mutableListOf()

    var bluetoothd: Bluetoothd? = null

    var isDone = false
        get() = field && mqttd?.isClientDone ?: false
        set(value) {
            field = value
            isClosed = value
        }

    var isClosed = false

    init {
        start()
    }

    private fun start() {
        if (dashboard.settings.mqttEnabled) {
            mqttd = Mqttd(context, dashboard.settings.mqttURI)

            for (t in dashboard.tiles) {
                for (st in t.mqttTopics.sub.get()) {
                    if (!mqttdSubTopics.contains(st)) mqttdSubTopics.add(st)
                }
            }

            mqttd?.onConnect?.observe(context as LifecycleOwner, { isConnected ->
                if (isConnected) {
                    for (st in mqttdSubTopics) mqttd?.subscribe(st)
                }
            })
        }

        if (dashboard.settings.bluetoothEnabled) bluetoothd = Bluetoothd()
    }

    fun stop() {
        mqttd?.stop()
        mqttdSubTopics.clear()
    }
}