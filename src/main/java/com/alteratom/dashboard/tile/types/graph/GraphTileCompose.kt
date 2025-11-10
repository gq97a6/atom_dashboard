package com.alteratom.tile.types.color.compose

import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication

object GraphTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        TilePropertiesComposeComponents.CommunicationBox {
            Communication()
        }
        TilePropertiesComposeComponents.Notification(fragment)
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}