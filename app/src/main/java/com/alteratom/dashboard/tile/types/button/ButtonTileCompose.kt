package com.alteratom.tile.types.button.compose

import androidx.compose.runtime.Composable
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropType

object ButtonTileCompose : TilePropType {
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