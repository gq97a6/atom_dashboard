package com.alteratom.dashboard.daemon

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.`object`.G.dashboards

//Manages creation and assignment of daemons
object DaemonsManager {

    fun notifyAllAssigned(context: Context) = dashboards.forEach { notifyAssigned(it, context) }

    fun notifyAllDischarged() = dashboards.forEach { notifyDischarged(it) }

    fun notifyAssigned(dashboard: Dashboard, context: Context) = try {
        dashboard.apply {
            daemon = Daemon(context, this, type)
            daemon?.notifyAssigned()
        }
    } catch (e: Exception) {
        println(e.toString())
    }

    fun notifyDischarged(dashboard: Dashboard) = try {
        dashboard.daemon?.notifyDischarged()
    } catch (_: Exception) {
    }

}