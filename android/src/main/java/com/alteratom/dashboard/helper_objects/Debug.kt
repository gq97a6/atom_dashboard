package com.alteratom.dashboard.helper_objects

import io.sentry.Sentry

object Debug {

    fun log(payload: String) {
        RemoteLog.log(payload)
        Sentry.addBreadcrumb(payload)
    }

    fun recordException(exception: Exception) {
        RemoteLog.log(exception.message ?: "EXCEPTION")
        Sentry.captureException(exception)
    }
}