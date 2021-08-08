package com.netDashboard.foreground_service

import android.content.Context
import android.util.Log
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.foreground_service.demons.Mqttd

class DaemonGroups(val context: Context) {

    private val dashboards = Dashboards.getList()
    private val list: MutableList<DaemonGroup> = mutableListOf()

    init {
        for (d in dashboards) list.add(DaemonGroup(context, d))
    }

    fun get(name: String): DaemonGroup? {
        for (dg in list) if (dg.dashboard.name == name) return dg

        return null
    }

    fun notifyDashboardRemoved(dashboardId: Long) {
        list.find { it.dashboard.id == dashboardId }?.deprecate()
        //todo:fix
    }


    fun notifyDashboardAdded(dashboard: Dashboard) {
        val dg = DaemonGroup(context, dashboard)
        list.add(dg)
        dashboard.daemonGroup = dg
    }
}

class DaemonGroup(context: Context, val dashboard: Dashboard) {
    var isDeprecated = false
    var mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true
        mqttd.conHandler.dispatch()
    }
}