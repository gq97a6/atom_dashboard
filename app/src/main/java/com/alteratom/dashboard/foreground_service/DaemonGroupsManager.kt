package com.alteratom.dashboard.foreground_service

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.G.dashboards

class DaemonGroupsManager(val context: Context) {

    private val list: MutableList<DaemonGroup> = mutableListOf()

    init {
        dashboards.forEach { list.add(DaemonGroup(context, it)) }
        assign()
    }

    fun get(id: Long): DaemonGroup? = list.find { it.dashboard.id == id }

    fun assign() {

        //Pair dashboards and daemonGroups
        dashboards.forEach { d ->
            list.find { it.dashboard.id == d.id }.let { dg ->
                if (dg != null && !dg.isDeprecated) {
                    d.dg = dg
                    dg.mqttd.d = d
                    dg.mqttd.notifyNewAssignment()
                } else {
                    notifyDashboardNew(d)
                }
            }
        }

        val assigned = dashboards.map { it.dg }

        //Deprecate not paired
        list.forEach { if (it !in assigned) it.deprecate() }

        //Remove not paired
        list.removeIf { it.isDeprecated }
    }

    fun notifyDashboardRemoved(dashboard: Dashboard) {
        dashboard.dg?.deprecate()
        list.remove(dashboard.dg)
    }

    fun notifyDashboardNew(dashboard: Dashboard) {
        val dg = DaemonGroup(context, dashboard)
        list.add(dg)
        dashboard.dg = dg
    }

    fun deprecateAll() {
        for (dg in list) dg.deprecate()
    }
}

class DaemonGroup(val context: Context, val dashboard: Dashboard) {
    var isDeprecated = false
    val mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true

        mqttd.isEnabled = false
        mqttd.conHandler.dispatch("dem_grp_dep")
    }
}