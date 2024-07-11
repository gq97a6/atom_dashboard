package com.alteratom.dashboard.daemon

import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.helper_objects.Debug

//Manages creation and assignment of daemons
object DaemonsManager {

    fun assignAll(context: Context) = aps.dashboards.forEach { assign(it, context) }

    fun dischargeAll() {
        Debug.log("DM_DISCHARGE_ALL")
        aps.dashboards.forEach { discharge(it) }
    }

    fun assign(dashboard: Dashboard, context: Context) = try {
        Debug.log("DM_ASSIGN")

        dashboard.apply {
            daemon = Daemon(context, this, type)
            daemon?.notifyAssigned()
        }
    } catch (e: Exception) {
        Debug.recordException(e)
    }

    fun discharge(dashboard: Dashboard) = try {
        Debug.log("DM_DISCHARGE")
        dashboard.daemon?.notifyDischarged()
    } catch (e: Exception) {
        Debug.recordException(e)
    }

}