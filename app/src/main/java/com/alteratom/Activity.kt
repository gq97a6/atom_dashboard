package com.alteratom

import android.app.Activity
import com.alteratom.Settings.Companion.saveToFile
import com.alteratom.Theme.Companion.saveToFile
import com.alteratom.dashboard.Dashboard.Companion.saveToFile
import com.alteratom.foreground_service.ForegroundService.Companion.service
import com.alteratom.G.dashboards
import com.alteratom.G.settings
import com.alteratom.G.theme

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
