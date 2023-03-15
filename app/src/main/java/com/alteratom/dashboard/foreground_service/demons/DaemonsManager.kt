package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.foreground_service.ForegroundService

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