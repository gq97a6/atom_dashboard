package com.alteratom.dashboard.manager

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class StatusManager(val context: Context, private val interval: Long = 1000) {

    private var job: Job? = null

    var isWorking = false

    //Start the manager if not already running
    fun dispatch(cancel: Boolean = false, reason: String = "") {
        //Cancel previous job if required
        if (cancel) job?.cancel()

        //Return if already dispatched
        if (job != null && job?.isActive == true) return

        //Launch job
        (context as? LifecycleOwner)?.apply {
            job = lifecycleScope.launch(Dispatchers.IO) {
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
                    onException(e)
                    delay(1000)
                    dispatch(true)
                }
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