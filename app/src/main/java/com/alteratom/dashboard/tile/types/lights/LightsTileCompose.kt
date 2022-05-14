package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.runtime.Composable
import com.alteratom.dashboard.compose.ComposeObject

object LightsTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}