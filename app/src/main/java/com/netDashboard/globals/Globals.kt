package com.netDashboard.globals

import com.google.gson.Gson
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Theme

object G {
    val gson = Gson()

    var settings = Settings()//Settings.getSaved()
    var theme = Theme()//Theme.getSaved()
    var dashboards = mutableListOf<Dashboard>()//Dashboards.getSaved()
}

