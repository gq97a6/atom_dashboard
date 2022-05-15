package com.alteratom.tile.types.color.compose

import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import androidx.compose.runtime.Composable
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication
import com.alteratom.dashboard.compose.ComposeObject

object GraphTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}