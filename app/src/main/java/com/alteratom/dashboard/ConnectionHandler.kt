package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

abstract class ConnectionHandler(private var interval: Long = 500) {

    val isDone = MutableLiveData(false)
    private var isDispatchScheduled = false

    fun dispatch(reason: String) {
        val _isDone = isDone()
        if (isDone.value != _isDone) isDone.postValue(_isDone)

        if (!_isDone && !isDispatchScheduled) {
            handleDispatch()
            Handler(Looper.getMainLooper()).postDelayed({
                isDispatchScheduled = false
                dispatch("internal")
            }, interval)
            isDispatchScheduled = true
        }
    }

    protected abstract fun isDone(): Boolean
    protected abstract fun handleDispatch()
}