package com.alteratom.dashboard

import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.FolderTree.parseSave
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.tile.Tile
import com.alteratom.dashboard.widgets.WidgetDataHolder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object G {
    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()
    lateinit var widgetDataHolder: WidgetDataHolder

    //Current
    var dashboardIndex = -2
    lateinit var dashboard: Dashboard
    lateinit var tile: Tile

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
        widgetDataHolder = parseSave() ?: WidgetDataHolder()
        dashboards = parseListSave()
        theme = parseSave() ?: Theme()
        settings = parseSave() ?: Settings()
    }
}

