package com.alteratom.tile.types.color.compose

import TilePropComp
import TilePropComp.PairList
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.pick.SelectTile

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

            val o = (tile as SelectTile).options
            PairList(
                o,
                { o.removeAt(it) },
                { o.add(Pair("", "")) },
                { i, v -> o[i] = o[i].copy(first = v) },
                { i, v -> o[i] = o[i].copy(second = v) },
            )
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}