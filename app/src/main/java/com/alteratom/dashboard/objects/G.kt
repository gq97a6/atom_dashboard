package com.alteratom.dashboard.objects

import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.objects.Storage.parseListSave
import com.alteratom.dashboard.objects.Storage.parseSave
import com.alteratom.dashboard.tile.Tile

object G {
    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    //Current
    var dashboardIndex = -2
    lateinit var dashboard: Dashboard
    lateinit var tile: Tile
    val unlockedDashboards = mutableListOf<Long>()

    fun setCurrentDashboard(index: Int): Boolean {
        return if (index !in 0 until dashboards.size) false
        else {
            dashboard = dashboards[index]
            dashboardIndex = index
            true
        }
    }

    fun setCurrentDashboard(id: Long): Boolean {
        dashboard = dashboards.find { it.id == id } ?: return false
        dashboardIndex = dashboards.indexOf(dashboard)
        return true
    }

    fun initialize() {
        dashboards = parseListSave()
        theme = parseSave() ?: Theme()
        settings = parseSave() ?: Settings()
        Pro.updateStatus()
    }
}

