package com.alteratom.dashboard

import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.FolderTree.parseSave
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
        return if (index !in 0..dashboards.size - 1) false
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
        ProVersion.updateStatus()
    }
}

