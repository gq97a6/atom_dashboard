package com.netDashboard.globals

import com.google.gson.Gson
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Theme

object G {
    val gson = Gson()

    var settings = Settings()
    var theme = Theme()
    var dashboards = mutableListOf<Dashboard>()

    fun initialize() {
        settings = Settings.getSaved()
        theme = Theme.getSaved()
        dashboards = Dashboards.getSaved()
    }
}

