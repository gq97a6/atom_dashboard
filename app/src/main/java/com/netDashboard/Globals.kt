package com.netDashboard.globals

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netDashboard.Settings
import com.netDashboard.Theme
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.tile.Tile

object G {
    val mapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    //Current
    lateinit var dashboard: Dashboard
    lateinit var tile: Tile

    lateinit var setIconHSV: (FloatArray) -> Unit
    lateinit var setIconKey: (String) -> Unit
    lateinit var getIconRes: () -> Int
    lateinit var getIconHSV: () -> FloatArray
    lateinit var getIconColorPallet: () -> ColorPallet


    fun setCurrentDashboard(id: Long): Boolean {
        dashboard = dashboards.find { it.id == id } ?: Dashboard(isInvalid = true)
        return !dashboard.isInvalid
    }

    fun initialize() {
        dashboards = Dashboard.parseSave() ?: mutableListOf()
        theme = Theme.parseSave() ?: Theme()
        settings = Settings.parseSave() ?: Settings()
    }
}

