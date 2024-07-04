package com.alteratom.dashboard.daemon

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.objects.Debug
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.objects.Setup

//Manages creation and assignment of daemons
object DaemonsManager {

    fun notifyAllAssigned(context: Context) = dashboards.forEach { notifyAssigned(it, context) }

    fun notifyAllDischarged() {
        Debug.log("DM_DISCHARGE_ALL")
        dashboards.forEach { notifyDischarged(it) }
    }

    fun notifyAssigned(dashboard: Dashboard, context: Context) = try {
        Debug.log("DM_ASSIGN[${context.javaClass}]")

        dashboard.apply {
            daemon = Daemon(context, this, type)
            daemon?.notifyAssigned()
        }
    } catch (e: Exception) {
        Debug.recordException("daeMan1", e)
    }

    fun notifyDischarged(dashboard: Dashboard) = try {
        Debug.log("DM_DISCHARGE")
        dashboard.daemon?.notifyDischarged()
    } catch (e: Exception) {
        Debug.recordException("daeMan2", e)
    }

}