import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication0
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication1
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.objects.G.tile

object SliderTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = tile as SliderTile

        TilePropertiesComposeComponents.Box {
            TilePropertiesComposeComponents.CommunicationBox {
                Communication0()

                var pub by remember { mutableStateOf(tile.mqtt.payloads["base"] ?: "@value") }
                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqtt.payloads["base"] = it
                    }
                )
                Text(
                    "Use @value to insert current tile value",
                    fontSize = 13.sp,
                    color = colors.a
                )

                Communication1()
            }

            TilePropertiesComposeComponents.Notification(fragment)

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
                        onValueChange = { it ->
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
                        onValueChange = { it ->
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
                        onValueChange = { it ->
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
    override fun Bluetoothd(fragment: Fragment) {
    }
}