package com.netDashboard.main

import com.netDashboard.dashboard.Dashboard
import java.io.File

class Dashboards {

    fun get(rootPath: String): MutableList<Dashboard> {

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames(rootPath)) {
            list.add(Dashboard(rootPath, name))
        }

        return list
    }

    fun getNames(rootPath: String): List<String> {

        val list: MutableList<String> = mutableListOf()
        File("$rootPath/dashboard_data").list()?.forEach {
            list.add(it.toString().substringAfterLast('/'))
        }

        return list
    }
}