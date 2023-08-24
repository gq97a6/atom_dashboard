package com.alteratom.dashboard.daemon

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.objects.G.dashboards

//Manages creation and assignment of daemons
object DaemonsManager {

    fun notifyAllAdded(context: Context) = dashboards.forEach { notifyAdded(it, context) }

    fun notifyAllRemoved() = dashboards.forEach { it.daemon?.notifyDischarged() }

    fun notifyAdded(dashboard: Dashboard, context: Context) = try {
        dashboard.apply {
            daemon = Daemon(context, this, type)
            daemon?.notifyAssigned()
        }
    } catch (e: Exception) {
        println(e.toString())
    }

    fun notifyRemoved(dashboard: Dashboard) = try {
        dashboard.daemon?.notifyDischarged()
    } catch (_: Exception) {
    }

}