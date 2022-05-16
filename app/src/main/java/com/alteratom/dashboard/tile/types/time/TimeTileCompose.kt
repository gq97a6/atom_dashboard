package com.alteratom.tile.types.color.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropType
import com.alteratom.tile.types.time.TimeTile

object TimeTileCompose : TilePropType {
    @Composable
    override fun Mqttd() {
        val tile = tile as TimeTile

        var type by remember { mutableStateOf(if (tile.isDate) 0 else 1) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                Communication0()

                var pub by remember {
                    mutableStateOf(
                        tile.mqtt.payloads[if (type == 0) "time" else "date"] ?: ""
                    )
                }
                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqtt.payloads[if (type == 0) "time" else "date"] = it
                    }
                )
                Text(
                    if (type == 0) "Use @hour and @minute to insert current values" else "Use @day, @month, @year to insert current values.",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Communication1()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "time") {
                Column {

                    AnimatedVisibility(
                        visible = type == 0, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            var military by remember { mutableStateOf(tile.isMilitary) }
                            LabeledSwitch(
                                label = {
                                    Text(
                                        "24-hour clock:",
                                        fontSize = 15.sp,
                                        color = Theme.colors.a
                                    )
                                },
                                checked = military,
                                onCheckedChange = {
                                    military = it
                                    tile.isMilitary = it
                                },
                            )
                        }
                    }

                    HorizontalRadioGroup(
                        listOf(
                            "Time",
                            "Date",
                        ),
                        "Payload type:",
                        type,
                        {
                            type = it
                            tile.isDate = it == 1
                        },
                    )
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}