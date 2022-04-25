package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import com.alteratom.dashboard.dashboard.Dashboard

class DaemonGroup(val context: Context, val dashboard: Dashboard) {
    var isDeprecated = false
    val mqttd = Mqttd(context, dashboard)

    fun deprecate() {
        isDeprecated = true

        mqttd.isEnabled = false
        mqttd.conHandler.dispatch("dem_grp_dep")
    }
}