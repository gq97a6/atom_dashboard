package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompse
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompse.PairList
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.tile.types.pick.SelectTile

object SelectTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as SelectTile

        TilePropertiesCompse.Box {
            TilePropertiesCompse.CommunicationBox {
                Communication()
            }

            TilePropertiesCompse.Notification()

            FrameBox(a = "Type specific: ", b = "select") {
                Row {

                    var show by remember { mutableStateOf(tile.showPayload) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = show,
                        onCheckedChange = {
                            show = it
                            tile.showPayload = it
                        }
                    )
                }
            }

            val o = tile.options
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