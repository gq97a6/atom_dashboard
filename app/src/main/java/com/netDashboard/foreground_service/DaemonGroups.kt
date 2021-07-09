package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.foreground_service.demons.Mqttd

class DaemonGroups(private val context: Context) {

    private val dashboards = Dashboards.get()
    private val collection: MutableList<DaemonGroup> = mutableListOf()

    init {
        start()
    }

    private fun start() {
        for (d in dashboards) {
            val g = DaemonGroup(context, d)
            collection.add(g)
        }
    }

    fun stop() {
        for (dg in collection) {
            dg.stop()
        }
    }

    fun get(name: String): DaemonGroup? {
        for (dg in collection) {
            if (dg.dashboard.name == name) return dg
        }
        return null
    }
}

class DaemonGroup(context: Context, val dashboard: Dashboard) {

    var mqttd = Mqttd(context, dashboard)

    init {
        start()
    }

    private fun start() {
        if (dashboard.mqttEnabled) mqttd.start()
    }

    fun stop() {
        mqttd.stop()
    }
}