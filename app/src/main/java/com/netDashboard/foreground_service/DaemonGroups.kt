package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Mqttd
import com.netDashboard.globals.G.dashboards

class DaemonGroups(val context: Context) {

    private val list: MutableList<DaemonGroup> = mutableListOf()

    init {
        for (d in dashboards) list.add(DaemonGroup(context, d))
    }

    fun get(name: String): DaemonGroup? {
        for (dg in list) if (dg.dashboard.name == name) return dg

        return null
    }

    fun notifyDashboardRemoved(dashboard: Dashboard) {
        dashboard.daemonGroup?.deprecate()
        list.remove(dashboard.daemonGroup)
    }

    fun notifyDashboardAdded(dashboard: Dashboard) {
        val dg = DaemonGroup(context, dashboard)
        list.add(dg)
        dashboard.daemonGroup = dg
    }

    fun deprecate() {
        for (dg in list) dg.deprecate()
    }
}

class DaemonGroup(context: Context, val dashboard: Dashboard) {
    private var isDeprecated = false
    var mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true

        mqttd.isEnabled = false
        mqttd.conHandler.dispatch("dem_grp_dep")
    }
}