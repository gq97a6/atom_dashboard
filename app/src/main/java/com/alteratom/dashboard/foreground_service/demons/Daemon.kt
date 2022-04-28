package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.IdGenerator
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.DaemonsManager
import com.fasterxml.jackson.annotation.JsonIgnore

abstract class Daemon : IdGenerator.Indexed {
    override val id = getNewId()
    var isDeprecated = false
    abstract var isEnabled: Boolean

    abstract val isDone: MutableLiveData<Boolean>
    abstract val status: Any

    @JsonIgnore
    lateinit var dg: DaemonsManager.DashboardGroup

    init {
        reportTakenId()
    }

    open fun deprecate() {
        isDeprecated = true
    }

    abstract fun initialize(context: Context)
    abstract fun notifyDashboardAssigned(dashboard: Dashboard)
    abstract fun notifyDashboardDischarged(dashboard: Dashboard)

    abstract class DaemonConnectionHandler {

        val isDone = MutableLiveData(false)
        private var isDispatchScheduled = false

        protected abstract fun isDone(): Boolean

        fun dispatch(reason: String) {
            val _isDone = isDone()
            if (isDone.value != _isDone) isDone.postValue(_isDone)

            if (!_isDone && !isDispatchScheduled) {

                handleDispatch()

                Handler(Looper.getMainLooper()).postDelayed({
                    isDispatchScheduled = false
                    dispatch("internal")
                }, 500)
                isDispatchScheduled = true
            }
        }

        abstract fun handleDispatch()
    }
}