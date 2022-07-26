package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

abstract class ConnectionHandler {

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