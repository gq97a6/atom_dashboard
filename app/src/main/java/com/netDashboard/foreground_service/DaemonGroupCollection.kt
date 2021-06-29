package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.DashboardSavedList

class DaemonGroupCollection(private val context: Context, private val rootPath: String) {

    private val dashboards = DashboardSavedList().get(rootPath)
    private val collection: MutableList<DaemonGroup> = mutableListOf()

    init {
        start()
    }

    private fun start() {
        for (d in dashboards) {
            val g = DaemonGroup(context, rootPath, d.name)
            collection.add(g)
        }
    }

    fun stop() {
        for (dg in collection) {
            dg.stop()
        }
    }
}