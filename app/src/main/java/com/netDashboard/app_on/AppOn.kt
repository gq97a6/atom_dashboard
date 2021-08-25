package com.netDashboard.app_on

import com.netDashboard.dashboard.Dashboards
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Theme

object AppOn {
    fun destroy() {
        Dashboards.save()
        Settings.save()
        Theme.save()
    }

    fun pause() {

    }
}
