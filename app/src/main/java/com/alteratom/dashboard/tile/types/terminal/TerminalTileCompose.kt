package com.alteratom.tile.types.color.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.RadioGroup
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompose
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose

object TerminalTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Communication0()

                var pub by remember { mutableStateOf(tile.mqtt.payloads["base"] ?: "") }
                var type by remember { mutableStateOf(if (tile.mqtt.varPayload) 0 else 1) }

                AnimatedVisibility(
                    visible = type == 1, enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        EditText(
                            label = { Text("Publish payload") },
                            value = pub,
                            onValueChange = {
                                pub = it
                                tile.mqtt.payloads["base"] = it
                            }
                        )
                    }
                }

                RadioGroup(
                    listOf(
                        "Variable (set on send)",
                        "Static (always the same)",
                    ), "Payload setting type",
                    type,
                    {
                        type = it
                        tile.mqtt.varPayload = it == 0
                    },
                    modifier = Modifier.padding(top = 20.dp)
                )

                Communication1()
            }
            TilePropertiesCompose.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}