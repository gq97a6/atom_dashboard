package com.alteratom.dashboard.objects

import android.app.Activity
import android.content.Intent
import com.alteratom.dashboard.ForegroundService.Companion.service
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.objects.Storage.saveToFile

object ActivityHandler {

    fun onCreate(activity: Activity, finish: Boolean = true) {
        if (finish) service?.finishAffinity = { activity.finishAffinity() }
    }

    fun onDestroy() {
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }

    fun onPause() {
        G.unlockedDashboards.clear()
        dashboards.saveToFile()
        settings.saveToFile()
        theme.saveToFile()
    }

    fun Activity.restart() = this.apply {
        startActivity(Intent(this, MainActivity::class.java))

        //Stab it
        finish()

        //Kill it
        finishAffinity()
    }
}
