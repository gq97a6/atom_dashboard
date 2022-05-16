package com.alteratom.tile.types.color.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompse
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.tile.types.color.ColorTile

object ColorTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as ColorTile

        var pub by remember { mutableStateOf(tile.mqtt.payloads[tile.colorType.toString()] ?: "") }
        var type by remember { mutableStateOf(tile.colorType) }

        TilePropertiesCompse.Box {
            TilePropertiesCompse.CommunicationBox {
                Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqtt.payloads[type.toString()] = it
                    }
                )
                Text(
                    "Use ${
                        when (type) {
                            0 -> "@h, @s, @v"
                            1 -> "@hex"
                            2 -> "@r, @g, @b"
                            else -> "@hex"
                        }
                    } to insert current value.",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Communication1()
            }

            TilePropertiesCompse.Notification()

            FrameBox(a = "Type specific: ", b = "color") {
                Column {

                    var paint by remember { mutableStateOf(tile.doPaint) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Paint tile:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = paint,
                        onCheckedChange = {
                            paint = it
                            tile.doPaint = it
                        },
                    )

                    var raw by remember { mutableStateOf(tile.paintRaw) }
                    AnimatedVisibility(
                        visible = paint, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            LabeledCheckbox(
                                label = {
                                    Text(
                                        "Paint with raw color (ignore contrast)",
                                        fontSize = 15.sp,
                                        color = Theme.colors.a
                                    )
                                },
                                checked = raw,
                                onCheckedChange = {
                                    raw = it
                                    tile.paintRaw = it
                                },
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    HorizontalRadioGroup(
                        listOf(
                            "HSV",
                            "HEX",
                            "RGB",
                        ),
                        "Type:",
                        type,
                        {
                            type = it
                            tile.colorType = it
                            pub = tile.mqtt.payloads[tile.colorType.toString()] ?: ""
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