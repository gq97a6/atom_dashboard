package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.dashboard.Dashboard

class Bluetoothd : Daemon() {
    override var isEnabled = false
    override val status = MutableLiveData(false)

    override fun initialize(context: Context) {
    }

    override fun deprecate() {
    }

    override fun notifyDashboardAssigned(dashboard: Dashboard) {
    }

    override fun notifyDashboardDischarged(dashboard: Dashboard) {
    }
}