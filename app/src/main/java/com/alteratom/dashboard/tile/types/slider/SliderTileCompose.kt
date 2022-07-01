package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompose
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.tile.types.slider.SliderTile

object SliderTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as SliderTile

        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Communication0()

                var pub by remember { mutableStateOf("false") }
                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqtt.payloads["base"] = it
                    }
                )
                Text(
                    "User @value to insert current value",
                    fontSize = 13.sp,
                    color = colors.a
                )

                Communication1()
            }

            TilePropertiesCompose.Notification()

            FrameBox(a = "Type specific: ", b = "slider") {
                Column {
                    var drag by remember { mutableStateOf(tile.dragCon) }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Drag to control:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = drag,
                            onCheckedChange = {
                                drag = it
                                tile.dragCon = it
                            }
                        )

                        Text(
                            "EXPERIMENTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.a,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    var from by remember { mutableStateOf(tile.range[0].toString()) }
                    EditText(
                        label = { Text("From value") },
                        value = from,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            from = it
                            it.toIntOrNull()?.let {
                                tile.range[0] = it
                            }
                            //it.let { raw ->
                            //    it.digitsOnly().let { parsed ->
                            //        if (raw != parsed) from = parsed
                            //        else parsed.toIntOrNull()?.let {
                            //            tile.range[0] = it
                            //        }
                            //    }
                            //}
                        }
                    )

                    var to by remember { mutableStateOf(tile.range[1].toString()) }
                    EditText(
                        label = { Text("To value") },
                        value = to,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            to = it
                            it.toIntOrNull()?.let {
                                tile.range[1] = it
                            }
                            //it.let { raw ->
                            //    it.digitsOnly().let { parsed ->
                            //        if (raw != parsed) to = parsed
                            //        else parsed.toIntOrNull()?.let {
                            //            tile.range[1] = it
                            //        }
                            //    }
                            //}
                        }
                    )

                    var step by remember { mutableStateOf(tile.range[2].toString()) }
                    EditText(
                        label = { Text("Step value") },
                        value = step,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            step = it
                            it.toIntOrNull()?.let {
                                tile.range[2] = it
                            }
                            //it.let { raw ->
                            //    it.digitsOnly().let { parsed ->
                            //        if (raw != parsed) step = parsed
                            //        else parsed.toIntOrNull()?.let {
                            //            tile.range[2] = it
                            //        }
                            //    }
                            //}
                        }
                    )
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}