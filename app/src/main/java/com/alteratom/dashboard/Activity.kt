package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.G.widgetDataHolder
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service

object Activity {

    fun onCreate(activity: Activity) {
        service?.finishAffinity = { activity.finishAffinity() }
    }

    fun onDestroy() {
        widgetDataHolder.saveToFile()
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }

    fun onPause() {
        widgetDataHolder.saveToFile()
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }
}
