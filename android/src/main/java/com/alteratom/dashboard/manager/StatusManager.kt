package com.alteratom.dashboard.manager

import com.alteratom.BuildConfig
import com.alteratom.dashboard.helper_objects.Debug
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class StatusManager(
    private val interval: Long = if (BuildConfig.DEBUG) 1500 else 300,
    private val debug: Boolean = false
) {

    private var job: Job? = null

    var isWorking = false

    //Start the manager if not already running
    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(cancel: Boolean = false, reason: String = "") {

        if (debug) Debug.log("SM_DISPATCH [$reason]")

        //Cancel previous job if required
        if (cancel) {
            if (debug) Debug.log("SM_CANCEL_JOB")
            job?.cancel()
        }

        //Return if already dispatched
        if (job != null && job?.isActive == true) {
            return
        }

        //Launch job
        job = GlobalScope.launch(Dispatchers.IO) {
            if (debug) Debug.log("SM_JOB_LAUNCH")
            try {
                //First iteration
                if (!isStable()) {
                    isWorking = true
                    onJobStart()
                    makeStable()
                    delay(interval)
                }

                //All other iterations
                while (!isStable()) {
                    makeStable()
                    delay(interval)
                }

                isWorking = false
                onJobDone()
                if (debug) Debug.log("SM_SETTLE")
            } catch (e: Exception) { //Create another coroutine after a delay
                Debug.recordException(e)
                onException(e)
                delay(interval)
                dispatch(true)
            }
        }
    }

    //Check if done
    abstract fun isStable(): Boolean

    //Try to stabilize the status
    abstract fun makeStable()

    open fun onJobStart() {}

    open fun onJobDone() {}

    open fun onException(e: Exception) {}
}