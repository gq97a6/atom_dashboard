package com.alteratom.tile.types.color.compose

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.LabeledCheckbox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompse
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompse.PairList
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose
import com.alteratom.tile.types.thermostat.ThermostatTile

object ThermostatTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as ThermostatTile
        var incHumi by remember { mutableStateOf(tile.includeHumiditySetpoint) }

        TilePropertiesCompse.Box {
            TilePropertiesCompse.CommunicationBox {

                var tempSub by remember { mutableStateOf(tile.mqtt.subs["temp"] ?: "") }
                EditText(
                    label = { Text("Temperature subscribe topic") },
                    value = tempSub,
                    onValueChange = {
                        tempSub = it
                        tile.mqtt.subs["temp"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    }
                )

                var tempSetSub by remember {
                    mutableStateOf(
                        tile.mqtt.subs["temp_set"] ?: ""
                    )
                }
                EditText(
                    label = { Text("Temperature setpoint subscribe topic") },
                    value = tempSetSub,
                    onValueChange = {
                        tempSetSub = it
                        tile.mqtt.subs["temp_set"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    }
                )

                var tempSetPub by remember {
                    mutableStateOf(
                        tile.mqtt.pubs["temp_set"] ?: ""
                    )
                }
                EditText(
                    label = { Text("Temperature setpoint publish topic") },
                    value = tempSetPub,
                    onValueChange = {
                        tempSetPub = it
                        tile.mqtt.pubs["temp_set"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    },
                    trailingIcon = {
                        IconButton(onClick = { tempSetPub = tempSetSub }) {
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "copy",
                                tint = colors.b
                            )
                        }
                    }
                )

                Divider(
                    color = colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                var humiSub by remember { mutableStateOf(tile.mqtt.subs["humi"] ?: "") }
                EditText(
                    label = { Text("Humidity subscribe topic") },
                    value = humiSub,
                    onValueChange = {
                        humiSub = it
                        tile.mqtt.subs["humi"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    }
                )

                AnimatedVisibility(
                    visible = incHumi, enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        var humiSetSub by remember {
                            mutableStateOf(
                                tile.mqtt.subs["humi_set"] ?: ""
                            )
                        }
                        EditText(
                            label = { Text("Humidity setpoint subscribe topic") },
                            value = humiSetSub,
                            onValueChange = {
                                humiSetSub = it
                                tile.mqtt.subs["humi_set"] = it
                                dashboard.daemon.notifyOptionsChanged()
                            }
                        )

                        var humiSetPub by remember {
                            mutableStateOf(
                                tile.mqtt.pubs["humi_set"] ?: ""
                            )
                        }
                        EditText(
                            label = { Text("Humidity setpoint publish topic") },
                            value = humiSetPub,
                            onValueChange = {
                                humiSetPub = it
                                tile.mqtt.pubs["humi_set"] = it
                                dashboard.daemon.notifyOptionsChanged()
                            },
                            trailingIcon = {
                                IconButton(onClick = { humiSetPub = humiSetSub }) {
                                    Icon(
                                        painterResource(R.drawable.il_file_copy),
                                        "copy",
                                        tint = colors.b
                                    )
                                }
                            }
                        )
                    }
                }

                Divider(
                    color = colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                var modeSub by remember { mutableStateOf(tile.mqtt.subs["mode"] ?: "") }
                EditText(
                    label = { Text("Mode subscribe topic") },
                    value = modeSub,
                    onValueChange = {
                        modeSub = it
                        tile.mqtt.subs["mode"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    }
                )

                var modePub by remember { mutableStateOf(tile.mqtt.pubs["mode"] ?: "") }
                EditText(
                    label = { Text("Mode publish topic") },
                    value = modePub,
                    onValueChange = {
                        modePub = it
                        tile.mqtt.pubs["mode"] = it
                        dashboard.daemon.notifyOptionsChanged()
                    },
                    trailingIcon = {
                        IconButton(onClick = { modePub = modeSub }) {
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "copy",
                                tint = colors.b
                            )
                        }
                    }
                )

                Communication1(retain = false, pointer = {

                    var tempJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["temp"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Temperature JSON pointer") },
                        value = tempJson,
                        onValueChange = {
                            tempJson = it
                            tile.mqtt.jsonPaths["temp"] = it
                        }
                    )

                    var tempSetJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["temp_set"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Temperature setpoint JSON pointer") },
                        value = tempSetJson,
                        onValueChange = {
                            tempSetJson = it
                            tile.mqtt.jsonPaths["temp_set"] = it
                        }
                    )

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    var humiJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["humi"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Humidity JSON pointer") },
                        value = humiJson,
                        onValueChange = {
                            humiJson = it
                            tile.mqtt.jsonPaths["humi"] = it
                        }
                    )

                    var humiSetJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["humi_set"] ?: ""
                        )
                    }
                    AnimatedVisibility(
                        visible = incHumi, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            EditText(
                                label = { Text("Humidity setpoint JSON pointer") },
                                value = humiSetJson,
                                onValueChange = {
                                    humiSetJson = it
                                    tile.mqtt.jsonPaths["humi_set"] = it
                                }
                            )
                        }
                    }

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    var modeJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["mode"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Mode JSON pointer") },
                        value = modeJson,
                        onValueChange = {
                            modeJson = it
                            tile.mqtt.jsonPaths["mode"] = it
                        }
                    )

                })
            }

            TilePropertiesCompse.Notification()

            FrameBox(a = "Type specific: ", b = "thermostat") {
                Column {
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
                        },
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    LabeledSwitch(
                        label = {
                            Text(
                                "Include humidity setpoint:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = incHumi,
                        onCheckedChange = {
                            incHumi = it
                            tile.includeHumiditySetpoint = it
                        },
                    )

                    Text(
                        "Retain messages:",
                        fontSize = 15.sp,
                        color = colors.a,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    var tempRet by remember { mutableStateOf(tile.retain[0]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Temperature setpoint",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = tempRet,
                        onCheckedChange = {
                            tempRet = it
                            tile.retain[0] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    var humiRet by remember { mutableStateOf(tile.retain[1]) }
                    AnimatedVisibility(
                        visible = incHumi, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            LabeledCheckbox(
                                label = {
                                    Text(
                                        "Humidity setpoint",
                                        fontSize = 15.sp,
                                        color = colors.a
                                    )
                                },
                                checked = humiRet,
                                onCheckedChange = {
                                    humiRet = it
                                    tile.retain[1] = it
                                },
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }

                    var modeRet by remember { mutableStateOf(tile.retain[2]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Mode",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = modeRet,
                        onCheckedChange = {
                            modeRet = it
                            tile.retain[2] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    var humiStep by remember { mutableStateOf(tile.humidityStep.toString()) }
                    AnimatedVisibility(
                        visible = incHumi, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            EditText(
                                label = { Text("Humidity setpoint step") },
                                value = humiStep,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                onValueChange = {
                                    humiStep = it
                                    tile.humidityStep = it.toFloatOrNull() ?: 5f
                                },
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }

                    var tempStep by remember { mutableStateOf(tile.temperatureStep.toString()) }
                    EditText(
                        label = { Text("Temperature setpoint step") },
                        value = tempStep,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            tempStep = it
                            tile.temperatureStep = it.toFloatOrNull() ?: .5f
                        }
                    )

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    var tempFrom by remember { mutableStateOf(tile.temperatureRange[0].toString()) }
                    EditText(
                        label = { Text("Temperature setpoint from value") },
                        value = tempFrom,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            tempFrom = it
                            tile.temperatureRange[0] = it.toIntOrNull() ?: 15
                        }
                    )

                    var tempTo by remember { mutableStateOf(tile.temperatureRange[1].toString()) }
                    EditText(
                        label = { Text("Temperature setpoint to value") },
                        value = tempTo,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            tempTo = it
                            tile.temperatureRange[1] = it.toIntOrNull() ?: 30
                        }
                    )
                }
            }

            val m = tile.modes
            PairList(
                m,
                { m.removeAt(it) },
                { m.add(Pair("", "")) },
                { i, v -> m[i] = m[i].copy(first = v) },
                { i, v -> m[i] = m[i].copy(second = v) },
            )
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}