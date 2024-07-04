package com.alteratom.dashboard.objects

import com.alteratom.BuildConfig
import com.google.firebase.analytics.logEvent

object Debug {
    //private val log = mutableListOf<String>()

    fun log(payload: String) {
        if (BuildConfig.DEBUG) {
            G.analytics.logEvent(payload) {
                //param("PAYLOAD", payload)
            }
        }
    }

    fun recordException(
        where: String,
        what: Exception,
        doReport: Boolean = false,
        doLog: Boolean = false
    ) {
        if (BuildConfig.DEBUG) {
            G.analytics.logEvent("EXCEPTION") {
                param("WHERE", where)
                param("EXCEPTION", what.message ?: "")
            }
            //G.crashlytics.log(where)
            //G.crashlytics.recordException(what)
        }
    }
}