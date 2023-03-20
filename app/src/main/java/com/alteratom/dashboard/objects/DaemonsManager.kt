package com.alteratom.dashboard.objects

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.demons.Daemon

object DaemonsManager {

    fun notifyAllAdded() = dashboards.forEach { notifyAdded(it) }

    fun notifyAllRemoved() = dashboards.forEach { it.daemon.notifyDischarged() }

    fun notifyAdded(dashboard: Dashboard) = try {
        dashboard.apply {
            daemon = Daemon(ForegroundService.service as Context, this, type)
            daemon.notifyAssigned()
        }
    } catch (_: Exception) {
    }

    fun notifyRemoved(dashboard: Dashboard) = try {
        dashboard.daemon.notifyDischarged()
    } catch (_: Exception) {
    }

}