package com.netDashboard.globals

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netDashboard.Settings
import com.netDashboard.Theme
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards

object G {
    val mapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()
    lateinit var dashboard: Dashboard

    fun setCurrentDashboard(id: Long): Boolean {
        dashboard = dashboards.find { it.id == id } ?: Dashboard(isInvalid = true)
        return !dashboard.isInvalid
    }

    fun initialize() {
        settings = Settings.getSaved()
        theme = Theme.getSaved()
        dashboards = Dashboards.getSaved()
    }
}

