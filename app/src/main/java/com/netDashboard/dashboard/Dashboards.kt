package com.netDashboard.dashboard

import android.util.Log
import com.netDashboard.FolderTree.dashboardsFile
import com.netDashboard.createToast
import com.netDashboard.globals.G.mapper
import java.io.File
import java.io.FileReader


class Dashboards {
    companion object {
        fun getSaved(): MutableList<Dashboard> =
            try {
                mapper.readerForListOf(Dashboard::class.java).readValue(FileReader(dashboardsFile))
            } catch (e: Exception) {
                Log.i("OUY", "$e")
                mutableListOf()
            }

        fun MutableList<Dashboard>.save() {
            try {
                File(dashboardsFile).writeText(mapper.writeValueAsString(this))
            } catch (e: Exception) {
                run { }
            }
        }
    }
}
//2022-01-03 20:42:17.821 14523-14523/com.netDashboard.dev I/OUY: [{"name":"1305546190","isInvalid":false,"id":1078412368450186590,"bluetoothEnabled":false,"log":{"list":[]},"mqttAddress":"tcp://","mqttClientId":"316117819","mqttEnabled":true,"mqttPass":null,"mqttPort":1883,"mqttUserName":null,"tiles":[],"layout":2131558459,"mqttURI":"tcp://:1883"}]
