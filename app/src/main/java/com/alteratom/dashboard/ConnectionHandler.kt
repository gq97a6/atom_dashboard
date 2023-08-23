@file:Suppress("LocalVariableName")

package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

//TODO: remove
abstract class ConnectionHandler(private var interval: Long = 500) {

    private val isDone = MutableLiveData(false)
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
}