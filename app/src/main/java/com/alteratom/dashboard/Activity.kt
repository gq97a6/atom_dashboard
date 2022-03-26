package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.Settings.Companion.saveToFile
import com.alteratom.dashboard.Theme.Companion.saveToFile
import com.alteratom.dashboard.Dashboard.Companion.saveToFile
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme

object Activity {

    fun onCreate(activity: Activity) {
        service?.finishAffinity = { activity.finishAffinity() }
    }

    fun onDestroy() {
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }

    fun onPause() {
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }
}
