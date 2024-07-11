package com.alteratom.dashboard.helper_objects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object PingDebug {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.SECONDS)
        .build()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            while(true) {
                try {
                    val request = Request.Builder()
                        .url("https://logger.hostunit.net/1438185342")
                        .post("".toRequestBody())
                        .build()

                    client.newCall(request).execute()
                } catch (_: Exception) {
                }
                delay(10000)
            }
        }
    }
}