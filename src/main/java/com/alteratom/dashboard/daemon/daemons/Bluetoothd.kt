package com.alteratom.dashboard.daemon.daemons

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon

class Bluetoothd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {
    override var isEnabled = false
    override val statePing = MutableLiveData(null as String?)
    override val state = MutableLiveData(false)
}