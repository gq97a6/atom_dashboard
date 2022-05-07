package com.alteratom.dashboard.compose

import androidx.compose.runtime.Composable
import com.alteratom.dashboard.foreground_service.demons.Daemon

interface ComposeObject {
    @Composable
    fun compose(type: Daemon.Type) {
        when(type) {
            Daemon.Type.MQTTD -> Mqttd()
            Daemon.Type.BLUETOOTHD -> Bluetoothd()
        }
    }

    @Composable
    fun Mqttd()

    @Composable
    fun Bluetoothd()
}