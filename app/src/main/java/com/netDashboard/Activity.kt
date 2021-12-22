package com.netDashboard

import android.app.Activity
import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme

object Activity {

    fun onCreate(activity: Activity) {
        service?.finishAffinity = { activity.finishAffinity() }
    }

    fun onDestroy() {
        dashboards.save()
        settings.save()
        theme.save()
    }

    fun onPause() {
        dashboards.save()
        settings.save()
        theme.save()
    }
}
