package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.foreground_service.demons.Mqttd
import com.netDashboard.globals.G.dashboards

class DaemonGroupsManager(val context: Context) {

    private val list: MutableList<DaemonGroup> = mutableListOf()

    init {
        dashboards.forEach { list.add(DaemonGroup(context, it)) }
        assign()
    }

    fun get(id: Long): DaemonGroup? = list.find { it.dashboard.id == id }

    fun assign() {
        val assigned = mutableListOf<Int>()

        //Pair dashboards and daemonGroups
        dashboards.forEach { d ->
            list.find { it.dashboard.id == d.id }.let { dg ->
                if (dg != null && !dg.isDeprecated) {
                    d.dg = dg
                    dg.mqttd.d = d
                    dg.mqttd.notifyNewAssignment()
                    assigned.add(list.indexOf(dg))
                } else notifyDashboardAdded(d)
            }
        }

        //Deprecate not paired
        for (i in 0..list.size - 1) {
            if (i !in assigned) notifyDashboardRemoved(list[i].mqttd.d)
        }

        run {}
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

class DaemonGroup(val context: Context, val dashboard: Dashboard) {
    var isDeprecated = false
    val mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true

        mqttd.isEnabled = false
        mqttd.conHandler.dispatch("dem_grp_dep")
    }
}