package com.alteratom.dashboard.helper_objects

import com.alteratom.dashboard.manager.StatusManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object RemoteLog {
    private val manager = Manager()
    private val logsToSend = mutableListOf<String>()

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .build()

    fun log(payload: String) {
        logsToSend.add(payload)
        manager.dispatch()
    }

    class Manager : StatusManager(100) {
        override fun isStable() = logsToSend.isEmpty()

        override fun makeStable() {
            val payload = logsToSend.removeFirst()
            val request = Request.Builder()
                .url("https://logger.hostunit.net/3958935417")
                .post(payload.toRequestBody())
                .build()

            client.newCall(request).execute()
        }
    }
}