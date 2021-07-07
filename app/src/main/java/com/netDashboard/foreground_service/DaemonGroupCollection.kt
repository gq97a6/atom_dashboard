package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboards

class DaemonGroupCollection(private val context: Context) {

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