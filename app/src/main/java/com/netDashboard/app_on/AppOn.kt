package com.netDashboard.app_on

import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.globals.G.dashboards

object AppOn {
    fun destroy() {
        dashboards.save()
    }

    fun pause() {

    }
}
