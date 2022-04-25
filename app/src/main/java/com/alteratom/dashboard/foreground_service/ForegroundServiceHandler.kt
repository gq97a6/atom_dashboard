package com.alteratom.dashboard.foreground_service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.MutableLiveData


class ForegroundServiceHandler(var context: Context) {

    var isBounded: Boolean = false
    var service: MutableLiveData<ForegroundService?> = MutableLiveData(null)

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, IBinder: IBinder) {
            val binder = IBinder as ForegroundService.ForegroundServiceBinder
            service.postValue(binder.getService())
            isBounded = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBounded = false
        }
    }

    fun start() {
        Intent(context, ForegroundService::class.java).also {
            context.startForegroundService(it)
        }
    }

    fun stop() {
        Intent(context, ForegroundService::class.java).also {
            context.stopService(it)
        }
    }

    fun bind() {
        Intent(context, ForegroundService::class.java).also {
            context.bindService(it, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun unbind() {
        if (isBounded) {
            context.unbindService(connection)
            isBounded = false
        }
    }
}