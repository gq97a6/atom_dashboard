package com.netDashboard.foreground_service

import android.content.Context
import com.netDashboard.foreground_service.demons.Mqttd
import com.netDashboard.main.Dashboards

class DaemonGroupCollection(private val context: Context, rootPath: String) {

    private val dashboards = Dashboards().get(rootPath)
    private val mqttDaemons: MutableList<Mqttd> = mutableListOf()

    fun run() {

        for (d in dashboards) {

            if(d.settings.mqttAddress != "tcp://") {
                val mqttd = Mqttd(context, d.settings.mqttURI)
                mqttd.run()

                mqttDaemons.add(mqttd)
            }
        }
    }

    fun kill() {
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