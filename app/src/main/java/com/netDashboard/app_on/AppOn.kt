package com.netDashboard.app_on

import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme

object AppOn {
    fun destroy() {
        dashboards.save()
        settings.save()
        theme.save()
    }

    fun pause() {
        dashboards.save()
        settings.save()
        theme.save()
    }
}
