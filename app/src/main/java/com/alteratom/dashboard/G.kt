package com.alteratom.dashboard

import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object G {
    val mapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    //Current
    var dashboardIndex = -2
    lateinit var dashboard: Dashboard
    lateinit var tile: Tile

    lateinit var setIconHSV: (FloatArray) -> Unit
    lateinit var setIconKey: (String) -> Unit
    lateinit var getIconRes: () -> Int
    lateinit var getIconHSV: () -> FloatArray
    lateinit var getIconColorPallet: () -> ColorPallet

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
        dashboards = Dashboard.parseSave() ?: mutableListOf()
        theme = Theme.parseSave() ?: Theme()
        settings = Settings.parseSave() ?: Settings()
    }
}

