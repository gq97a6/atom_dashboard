package com.alteratom.dashboard.compose_daemon

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.daemon.Daemon

interface DaemonBasedCompose {
    @Composable
    fun Compose(type: Daemon.Type, fragment: Fragment) {
        when (type) {
            Daemon.Type.MQTTD -> Mqttd(fragment)
            Daemon.Type.BLUETOOTHD -> Bluetoothd(fragment)
        }
    }

    @Composable
    fun Compose(type: Daemon.Type) {
        when (type) {
            Daemon.Type.MQTTD -> Mqttd()
            Daemon.Type.BLUETOOTHD -> Bluetoothd()
        }
    }

    @Composable
    fun Mqttd(fragment: Fragment) {
    }

    @Composable
    fun Bluetoothd(fragment: Fragment) {
    }

    @Composable
    fun Mqttd() {
    }

    @Composable
    fun Bluetoothd() {
    }
}