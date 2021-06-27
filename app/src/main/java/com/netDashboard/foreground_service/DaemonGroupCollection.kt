package com.netDashboard.foreground_service

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.createNotification
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

            g.mqttd?.data?.observe(context as LifecycleOwner, { p ->
                if (p.first != null && p.second != null) {
                    createNotification(context, "${g.name}: ${p.first}", p.second.toString(), false)
                }
            })

            collection.add(g)
        }
    }

    private fun start(name: String) {
        val g = DaemonGroup(context, rootPath, name)

        g.mqttd?.data?.observe(context as LifecycleOwner, { p ->
            if (p.first != null && p.second != null && !g.isClosed) {
                createNotification(context, "${g.name}: ${p.first}", p.second.toString(), false)
            }
        })

        collection.add(g)
    }

    fun stop() {
        for (dg in collection) {
            dg.isDone = true
            dg.mqttd?.data?.removeObservers(context as LifecycleOwner)
            dg.stop()
        }

        clean()
    }

    private fun stop(name: String) {
        for (dg in collection) {
            if (dg.dashboard.name == name) {
                dg.isDone = true
                dg.mqttd?.data?.removeObservers(context as LifecycleOwner)
                dg.stop()
                break
            }
        }

        clean()
    }

    fun restart() {
        stop()
        start()
    }

    fun restart(name: String) {
        stop(name)
        start(name)
    }

    private fun clean() {
        var ii = 0
        for (i in 0 until collection.size) {
            if (collection[i - ii].isDone) {
                collection.removeAt(i - ii)
                ii++
            }
        }
    }

    fun get(name: String): DaemonGroup? {
        for (dg in collection) if(dg.dashboard.name == name) return dg
        return null
    }
}