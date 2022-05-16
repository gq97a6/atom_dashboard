package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import androidx.compose.runtime.Composable

interface DaemonBasedCompose {
    @Composable
    fun compose(type: Daemon.Type, context: Context) {
        when (type) {
            Daemon.Type.MQTTD -> Mqttd(context)
            Daemon.Type.BLUETOOTHD -> Bluetoothd(context)
        }
    }

    @Composable
    fun compose(type: Daemon.Type) {
        when (type) {
            Daemon.Type.MQTTD -> Mqttd()
            Daemon.Type.BLUETOOTHD -> Bluetoothd()
        }
    }

    @Composable
    fun Mqttd(context: Context) {
    }

    @Composable
    fun Bluetoothd(context: Context) {
    }

    @Composable
    fun Mqttd() {
    }

    @Composable
    fun Bluetoothd() {
    }
}