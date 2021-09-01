package com.netDashboard.app_on

import android.app.Activity
import androidx.lifecycle.LifecycleOwner
import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme

object AppOn {

    fun onCreate(activity: Activity) {
        service?.finishFlag?.observe(activity as LifecycleOwner) { flag ->
            if (flag) activity.finishAffinity()
        }
    }

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
