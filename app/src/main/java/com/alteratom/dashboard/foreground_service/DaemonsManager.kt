package com.alteratom.dashboard.foreground_service

import android.content.Context
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.dashboard.foreground_service.demons.Mqttd

class DaemonsManager(val context: Context) {

    private val list: MutableList<Daemon> = mutableListOf()
    //inline fun <T> getDaemon(id: Long?): T? = id?.let { list[it] } as? T?

    init {
        initialize()
    }

    fun initialize() {
        //dashboards.forEach { list.add(DaemonGroup(context, it)) }
        val d = Daemon<Mqttd>(context, dashboard)
        assign()
    }

    fun assign() {

        ////Pair dashboards and daemonGroups
        //dashboards.forEach { d ->
        //    list.find { it.dashboard.id == d.id }.let { dg ->
        //        if (dg != null && !dg.isDeprecated) {
        //            d.dg = dg
        //            dg.mqttd.d = d
        //            dg.mqttd.notifyNewAssignment()
        //        } else {
        //            notifyDashboardNew(d)
        //        }
        //    }
        //}
//
        //val assigned = dashboards.map { it.dg }
//
        ////Deprecate not paired
        //list.forEach { if (it !in assigned) it.deprecate() }
//
        ////Remove not paired
        //list.removeIf { it.isDeprecated }
    }

    //fun notifyDashboardAdded(dashboard: Dashboard) {
    //}

    //fun notifyDashboardRemoved(dashboard: Dashboard) {
    //}

    //fun deprecateAll() = list.values.forEach { it.deprecate() }

    //inline fun <reified D : Daemon> createDaemon() {
    //    val d = when (D::class) {
    //        Mqttd::class -> Mqttd(context)
    //        Bluetoothd::class -> Bluetoothd()
    //        else -> null
    //    }
//
    //    d?.let { list[d.id] = d }
    //}
}