package com.netDashboard.globals

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.settings.Settings
import com.netDashboard.theme.Theme

object G {
    val gson = Gson()
    val mapper: ObjectMapper =
        jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    fun initialize() {
        settings = Settings.getSaved()
        theme = Theme.getSaved()
        dashboards = Dashboards.getSaved()
    }
}

