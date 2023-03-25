@file:Suppress("LocalVariableName")

package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

abstract class ConnectionManager(private var interval: Long = 500) {

    val isDone = MutableLiveData(false)
    private var isDispatchScheduled = false

    fun dispatch(reason: String) {
        val isDoneResult = isDoneCheck()
        if (isDone.value != isDoneResult) isDone.postValue(isDoneResult)

        if (!isDoneResult && !isDispatchScheduled) {
            handleDispatch()
            Handler(Looper.getMainLooper()).postDelayed({
                isDispatchScheduled = false
                dispatch("internal")
            }, interval)
            isDispatchScheduled = true
        }
    }

    protected abstract fun isDoneCheck(): Boolean
    protected abstract fun handleDispatch()

    fun dismiss() {

    }
}