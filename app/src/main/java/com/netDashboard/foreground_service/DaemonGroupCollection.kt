package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.dashboard.DashboardSavedList

class DaemonGroupCollection(private val context: Context, private val rootPath: String) {

    private val dashboards = DashboardSavedList().get(rootPath)
    val daemonsGroups: MutableList<DaemonGroup> = mutableListOf()

    init {
        start()
    }

    private fun start() {
        for (d in dashboards) {
            daemonsGroups.add(DaemonGroup(context, rootPath, d.name))
        }
    }

    fun stop() {
        for (dg in daemonsGroups) {
            dg.stop()
        }
    }

    fun rerun() {
        for (dg in daemonsGroups) {
            dg.rerun()
        }
    }

    fun rerun(name: String) {
        for (dg in daemonsGroups) {
            if(dg.d.name == name) {
                dg.rerun()
                break
            }
        }
    }

    // mqttd.data.observe(context as LifecycleOwner, { p ->
    //     if (p.first != "R73JETTY") {
    //         Log.i("OUY", "MSG: ${p.second} | D: ${dashboard.name}")
    //         for (element in tiles) {
    //             element.onData(p.first, p.second)
    //         }
    //     }
    // })
}