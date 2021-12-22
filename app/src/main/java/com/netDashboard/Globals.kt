package com.netDashboard.globals

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.Settings
import com.netDashboard.Theme

object G {
    val mapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()
    lateinit var dashboard: Dashboard

    fun MutableList<Dashboard>.getById(id: Long): Dashboard =
        this.find { it.id == id } ?: Dashboard(isInvalid = true)

    fun setCurrentDashboard(id: Long) {
        dashboard = dashboards.find { it.id == id } ?: Dashboard(isInvalid = true)
    }

    fun initialize() {
        settings = Settings.getSaved()
        theme = Theme.getSaved()
        dashboards = Dashboards.getSaved()
    }
}

