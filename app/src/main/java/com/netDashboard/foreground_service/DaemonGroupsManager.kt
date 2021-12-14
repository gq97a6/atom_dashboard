package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Mqttd
import com.netDashboard.globals.G.dashboards

class DaemonGroupsManager(val context: Context) {

    private val list: MutableList<DaemonGroup> = mutableListOf()

    init {
        dashboards.forEach { list.add(DaemonGroup(context, it)) }
    }

    fun get(id: Long): DaemonGroup? = list.find { it.dashboard.id == id }

    fun assign() {
        dashboards.forEach { d ->
            list.find { it.dashboard.id == d.id }?.let {
                d.dg = it
                it.mqttd.d = d
                it.mqttd.reinit("assign")
            }
        }
    }

    fun notifyDashboardRemoved(dashboard: Dashboard) {
        dashboard.dg?.deprecate()
        list.remove(dashboard.dg)
    }

    fun notifyDashboardAdded(dashboard: Dashboard) {
        val dg = DaemonGroup(context, dashboard)
        list.add(dg)
        dashboard.dg = dg
    }

    fun deprecateAll() {
        for (dg in list) dg.deprecate()
    }
}

class DaemonGroup(context: Context, val dashboard: Dashboard) {
    private var isDeprecated = false
    val mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true

        mqttd.isEnabled = false
        mqttd.conHandler.dispatch("dem_grp_dep")
    }
}