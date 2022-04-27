package com.alteratom.dashboard.foreground_service

import android.content.Context
import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.dashboard.foreground_service.demons.Mqttd

class DaemonsManager(val context: Context) {

    //dashboard has non existing daemon id
    //daemon adding
    //ensure daemon is assigned

    val list: MutableMap<Long, Daemon>
    inline fun <T> getDaemon(id: Long?): T? = list[id ?: -1] as? T?

    init {
        //Get saved daemons list
        list = parseListSave<Daemon>().associateBy { it.id }.toMutableMap()

        assign()

        //Initialize daemons
        list.forEach { it.value.initialize() }
    }

    //Create daemons groups for dashboards
    fun assign(doFlush: Boolean = false) {

        if (doFlush) list.values.forEach {
            it.ds.list.removeAll { true }
        }

        //Create daemons groups for dashboards
        dashboards.forEach {
            it.dg = DaemonGroup(
                it,
                getDaemon(it.daemonsIds?.get(Mqttd::class))
            )
        }
    }

    //on dashboard removed
    fun notifyDashboardRemoved(dashboard: Dashboard) {
        //dashboard.dg?.deprecate()
        //list.remove(dashboard.dg)
    }

    //on dashboard added
    fun notifyDashboardNew(dashboard: Dashboard) {
        //val dg = DaemonGroup(context, dashboard)
        //list.add(dg)
        //dashboard.dg = dg
    }

    //on shutdown
    fun deprecateAll() {
        //for (dg in list) dg.deprecate()
    }

    class DaemonGroup(dashboard: Dashboard, val mqttd: Mqttd?) {
        init { //Add dashboard to daemon's list
            mqttd?.ds?.list?.add(dashboard)
        }
    }
}