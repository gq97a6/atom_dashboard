package com.alteratom.dashboard.demons

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard

class Bluetoothd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {
    override var isEnabled = false
    override val isDone = MutableLiveData(false)
    override val status = MutableLiveData(false)
}