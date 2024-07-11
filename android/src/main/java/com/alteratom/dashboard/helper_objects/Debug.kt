package com.alteratom.dashboard.helper_objects

import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit


object Debug {
    private val client: OkHttpClient = Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .build()

    //private val client: OkHttpClient = OkHttpClient()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun log(payload: String) {
        scope.launch { logRemote(payload) }
        Sentry.addBreadcrumb(payload)
    }

    fun recordException(exception: Exception) {
        Sentry.captureException(exception)
    }

    private fun logRemote(payload: String) {
        try {
            val request = Request.Builder()
                .url("https://logger.hostunit.net/3958935417")
                .post(payload.toRequestBody())
                .build()

            client.newCall(request).execute()
        } catch (e: Exception) {
        }
    }
}