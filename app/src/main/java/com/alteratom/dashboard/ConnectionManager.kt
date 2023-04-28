@file:Suppress("LocalVariableName")

package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.objects.Logger

abstract class ConnectionManager(private var interval: Long = 500) {

    val isDone = MutableLiveData(false)
    private var isDispatchScheduled = false

    fun dispatch(reason: String) {
        val isDoneResult = isDoneCheck()
        if (isDone.value != isDoneResult) isDone.postValue(isDoneResult)

        if (!isDoneResult && !isDispatchScheduled) {
            try {
                handleDispatch(reason)
            } catch (e: Exception) {
                Logger.log(e.stackTraceToString())
            }

            Handler(Looper.getMainLooper()).postDelayed({
                isDispatchScheduled = false
                dispatch("internal")
            }, interval)
            isDispatchScheduled = true
        }
    }

    //Check if stable or dispatch is needed
    protected abstract fun isDoneCheck(): Boolean
    protected abstract fun handleDispatch(reason: String)
}