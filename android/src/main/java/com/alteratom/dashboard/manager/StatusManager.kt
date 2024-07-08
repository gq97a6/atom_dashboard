package com.alteratom.dashboard.manager

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.alteratom.BuildConfig
import com.alteratom.dashboard.helper_objects.Debug
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class StatusManager(private val interval: Long = if (BuildConfig.DEBUG) 1500 else 300) {

    private var job: Job? = null

    var isWorking = false

    //Start the manager if not already running
    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(cancel: Boolean = false, reason: String = "") {

        Debug.log("SM_DISPATCH [$reason]")

        //Cancel previous job if required
        if (cancel) {
            Debug.log("SM_CANCEL_JOB")
            job?.cancel()
        }

        //Return if already dispatched
        if (job != null && job?.isActive == true) {
            return
        }

        //Launch job
        job = GlobalScope.launch(Dispatchers.IO) {
            Debug.log("SM_JOB_LAUNCH")
            try {
                //First iteration
                if (!check()) {
                    isWorking = true
                    onJobStart()
                    handle()
                    delay(interval)
                }

                //All other iterations
                while (!check()) {
                    handle()
                    delay(interval)
                }

                isWorking = false
                onJobDone()
            } catch (e: Exception) { //Create another coroutine after a delay
                Debug.recordException(e)
                onException(e)
                delay(interval)
                dispatch(true)
            }
        }
    }

    //Check if done
    abstract fun check(): Boolean

    //Try to stabilize the status
    abstract fun handle()

    open fun onJobStart() {}

    open fun onJobDone() {}

    open fun onException(e: Exception) {}
}