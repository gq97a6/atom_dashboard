package com.alteratom.dashboard.foreground_service

import android.content.Context
import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Bluetoothd
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.tile.Tile

class DaemonsManager(val context: Context) {

    lateinit var list: MutableMap<Long, Daemon>
    inline fun <T> getDaemon(id: Long?): T? = id?.let { list[it] } as? T?

    init {
        initialize()
    }

    fun initialize() {
        //Deprecate existing daemons
        deprecateAll()

        //Get saved daemons list
        list = parseListSave<Daemon>().associateBy { it.id }.toMutableMap()

        //Initialize daemons
        list.values.forEach {
            it.dg = DashboardGroup(it)
            it.initialize(context)
        }

        dashboards.forEach {
            it.daemon = getDaemon(it.id)
        }
    }

    inline fun <reified D : Daemon> createDaemon() {
        val d = when (D::class) {
            Mqttd::class -> Mqttd(context)
            Bluetoothd::class -> Bluetoothd()
            else -> null
        }

        d?.let { list[d.id] = d }
    }

    fun <D : Daemon> removeDaemon(daemon: D) {
        dashboards.forEach {
            it.daemonId = -1L
            it.daemon = null
        }
    }

    fun deprecateAll() = list.values.forEach { it.deprecate() }

    fun notifyDaemonAssign(daemon: Daemon, dashboard: Dashboard, doPass: Boolean = true) {
        dashboard.daemonId = daemon.id
        dashboard.daemon = daemon
        daemon.dg.setDashboard(dashboard)

        if (doPass) daemon.notifyDashboardAssigned(dashboard)
    }

    fun notifyDashboardDischarge(dashboard: Dashboard) {
        list.values.forEach {
            it.dg.list.remove(dashboard)
            it.notifyDashboardDischarged(dashboard)
        }
    }

    inner class DashboardGroup(daemon: Daemon) {
        val list = dashboards.filter { it.daemonId == daemon.id }.toMutableList()

        fun setDashboard(dashboard: Dashboard) {
            if (dashboard !in list) list.add(dashboard)
        }

        fun getTiles(): MutableList<Tile> =
            list.fold(mutableListOf(), { tiles, d ->
                tiles.addAll(d.tiles)
                return tiles
            })
    }

    //inner class DaemonGroup(dashboard: Dashboard) {
    //    var mqttd = null as Mqttd?
    //    init {
    //        list.values.filter { dashboard.daemonsIds.containsValue(it.id) }.forEach {
    //            setDaemon(it)
    //        }
    //    }
    //    inline fun setDaemon(daemon: Daemon?) {
    //        when (daemon) {
    //            is Mqttd? -> mqttd = daemon
    //        }
    //    }
    //}
}