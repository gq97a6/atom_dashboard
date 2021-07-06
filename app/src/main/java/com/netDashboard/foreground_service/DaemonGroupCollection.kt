package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboards

class DaemonGroupCollection(private val context: Context) {

    private val dashboards = Dashboards.list
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
}