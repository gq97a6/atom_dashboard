package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeObject

object SelectTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var state by remember { mutableStateOf(true) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "select") {
                Row {
                    LabeledSwitch(
                        label = {
                            Text(
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it }
                    )
                }
            }

            //val t = (tile as SelectTile)
            //TilePropComp.PairList(options = t.options)
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}