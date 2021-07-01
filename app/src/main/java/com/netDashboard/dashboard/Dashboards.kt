package com.netDashboard.dashboard

import com.netDashboard.folder_tree.FolderTree.dashboardsFolder
import java.io.File

object Dashboards {

    lateinit var list: MutableList<Dashboard>

    fun get(name: String): Dashboard? {
        for (d in list) {
            if (d.name == name) return d
        }

        return null
    }

    fun set() {

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames()) {
            list.add(Dashboard(name))
        }

        this.list = list
    }

    private fun getNames(): List<String> {

        val list: MutableList<String> = mutableListOf()

        File(dashboardsFolder).list()?.forEach {
            list.add(it.toString().substringAfterLast('/'))
        }

        return list
    }
}