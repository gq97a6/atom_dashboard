package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.ForegroundService

object DaemonsManager {

    fun initialize() {
        dashboards.forEach {
            notifyDashboardAdded(it)
        }

        notifyAllAssigned()
    }

    fun notifyAssigned(dashboard: Dashboard) =
        dashboard.daemon.notifyAssigned()

    fun notifyDischarged(dashboard: Dashboard) =
        dashboard.daemon.notifyDischarged()

    fun notifyAllAssigned() {
        dashboards.forEach {
            it.daemon.notifyAssigned()
        }
    }

    fun notifyAllDischarged() {
        dashboards.forEach {
            try {
                it.daemon.notifyDischarged()
            } catch (e: Exception) {
            }
        }
    }

    fun notifyDashboardAdded(dashboard: Dashboard) {
        dashboard.apply {
            daemon = Daemon(ForegroundService.service as Context, this, type)
            notifyAssigned(this)
        }
    }

    fun notifyDashboardRemoved(dashboard: Dashboard) = notifyDischarged(dashboard)
}