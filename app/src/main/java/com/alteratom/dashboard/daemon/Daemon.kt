package com.alteratom.dashboard.daemon

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.daemons.Bluetoothd
import com.alteratom.dashboard.daemon.daemons.Mqttd
import com.alteratom.dashboard.objects.IdentityGenerator
import com.alteratom.dashboard.objects.IdentityGenerator.obtainNewId
import com.alteratom.dashboard.objects.IdentityGenerator.reportTakenId

abstract class Daemon(val context: Context, var d: Dashboard) {

    val id = obtainNewId()

    var isDischarged = false
    protected abstract val isEnabled: Boolean

    abstract val isDone: MutableLiveData<Boolean>
    abstract val status: Any

    companion object {
        operator fun invoke(context: Context, dashboard: Dashboard, type: Type) =
            when (type) {
                Type.MQTTD -> Mqttd::class
                Type.BLUETOOTHD -> Bluetoothd::class
            }.constructors.first().call(context, dashboard)

        inline operator fun <reified D : Daemon> invoke(context: Context, dashboard: Dashboard) =
            D::class.constructors.first().call(context, dashboard)
    }

    init {
        reportTakenId(id)
    }

    //Notify daemon when it is assigned to a dashboard
    open fun notifyAssigned() {
        isDischarged = false
    }

    //Notify daemon when it is discharged from
    open fun notifyDischarged() {
        isDischarged = true
    }

    //Notify daemon when it's config has changed
    open fun notifyConfigChanged() {}

    enum class Type { MQTTD, BLUETOOTHD }
}