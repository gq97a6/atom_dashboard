package com.netDashboard.dashboard

import android.util.Log
import com.netDashboard.folder_tree.FolderTree.dashboardsFile
import com.netDashboard.globals.G.mapper
import java.io.File
import java.io.FileReader


class Dashboards {
    companion object {
        fun getSaved(): MutableList<Dashboard> =
            try {
                Log.i("OUY", "get ok")
                mapper.readerForListOf(Dashboard::class.java).readValue(FileReader(dashboardsFile))
            } catch (e: Exception) {
                Log.i("OUY", "$e")
                mutableListOf()
            }

        fun MutableList<Dashboard>.save() {
            try {
                Log.i("OUY", "save ok")
                File(dashboardsFile).writeText(mapper.writeValueAsString(this))
            } catch (e: Exception) {
                Log.i("OUY", "$e")
                run { }
            }
        }
    }
}