package com.alteratom.dashboard.helper_objects

import io.sentry.Sentry

object Debug {
    fun log(payload: String) {
        Sentry.addBreadcrumb(payload)
    }

    fun recordException(exception: Exception) {
        Sentry.captureException(exception)
    }
}