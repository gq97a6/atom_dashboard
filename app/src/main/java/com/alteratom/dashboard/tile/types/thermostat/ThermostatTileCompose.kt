package com.alteratom.tile.types.color.compose

import TilePropComp
import TilePropComp.PairList
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.LabeledCheckbox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.thermostat.ThermostatTile

object ThermostatTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {

                EditText(
                    label = { Text("Temperature subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Temperature setpoint subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Temperature setpoint publish topic") },
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(painterResource(R.drawable.il_file_copy), "", tint = colors.b)
                        }
                    }
                )

                Divider(
                    color = colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                EditText(
                    label = { Text("Humidity subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Humidity setpoint subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Humidity setpoint publish topic") },
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(painterResource(R.drawable.il_file_copy), "", tint = colors.b)
                        }
                    }
                )

                Divider(
                    color = colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                EditText(
                    label = { Text("Mode subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Mode publish topic") },
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(painterResource(R.drawable.il_file_copy), "", tint = colors.b)
                        }
                    }
                )

                TilePropComp.Communication1(retain = false, pointer = {

                    EditText(
                        label = { Text("Temperature JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("Temperature setpoint JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    EditText(
                        label = { Text("Humidity JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("Humidity setpoint JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    EditText(
                        label = { Text("Mode JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                })
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "thermostat") {
                Column {
                    LabeledSwitch(
                        label = {
                            Text(
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
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
                        checked = state,
                        onCheckedChange = { state = it },
                    )

                    Text(
                        "Retain messages:",
                        fontSize = 15.sp,
                        color = colors.a,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "Temperature setpoint",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "Humidity setpoint",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "Mode",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    EditText(
                        label = { Text("Humidity setpoint step") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    EditText(
                        label = { Text("Temperature setpoint step") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    Divider(
                        color = colors.b, thickness = 0.dp, modifier = Modifier
                            .padding(top = 10.dp)
                            .padding(vertical = 10.dp)
                    )

                    EditText(
                        label = { Text("Temperature setpoint from value") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("Temperature setpoint to value") },
                        value = text,
                        onValueChange = { text = it }
                    )
                }
            }

            val m = (tile as ThermostatTile).modes
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


//val tile = tile as ThermostatTile
//
//b.tpMqttRetainBox.visibility = GONE
//b.tpMqttTopics.visibility = GONE
//b.tpMqttJsonPayload.visibility = GONE
//b.tpThermostat.visibility = VISIBLE
//b.tpThermostatTopics.visibility = VISIBLE
//b.tpThermostatPaths.visibility =
//if (tile.mqtt.payloadIsJson) VISIBLE else GONE
//
//b.tpThermostatTemperatureSub.setText(tile.mqtt.subs["temp"])
//b.tpThermostatTemperatureSetpointSub.setText(tile.mqtt.subs["temp_set"])
//b.tpThermostatTemperatureSetpointPub.setText(tile.mqtt.pubs["temp_set"])
//b.tpThermostatHumiditySub.setText(tile.mqtt.subs["humi"])
//b.tpThermostatHumiditySetpointSub.setText(tile.mqtt.subs["humi_set"])
//b.tpThermostatHumiditySetpointPub.setText(tile.mqtt.pubs["humi_set"])
//b.tpThermostatModeSub.setText(tile.mqtt.subs["mode"])
//b.tpThermostatModePub.setText(tile.mqtt.pubs["mode"])
//
//b.tpThermostatTemperaturePath.setText(tile.mqtt.jsonPaths["temp"])
//b.tpThermostatTemperatureSetpointPath.setText(tile.mqtt.jsonPaths["temp_set"])
//b.tpThermostatHumidityPath.setText(tile.mqtt.jsonPaths["humi"])
//b.tpThermostatHumiditySetpointPath.setText(tile.mqtt.jsonPaths["humi_set"])
//b.tpThermostatModePath.setText(tile.mqtt.jsonPaths["mode"])
//
//b.tpThermostatHumidityStep.setText(tile.humidityStep.toString())
//b.tpThermostatTemperatureFrom.setText(tile.temperatureRange[0].toString())
//b.tpThermostatTemperatureTo.setText(tile.temperatureRange[1].toString())
//b.tpThermostatTemperatureStep.setText(tile.temperatureStep.toString())
//
//tile.includeHumiditySetpoint.let {
//    b.tpThermostatHumidityTopicsBox.visibility = if (it) VISIBLE else GONE
//    b.tpThermostatHumidityStepBox.visibility = if (it) VISIBLE else GONE
//    b.tpThermostatIncludeHumiditySetpoint.isChecked = it
//}
//
//b.tpThermostatShowPayload.isChecked = tile.showPayload
//b.tpThermostatTempRetain.isChecked = tile.retain[0]
//b.tpThermostatHumiRetain.isChecked = tile.retain[1]
//b.tpThermostatModeRetain.isChecked = tile.retain[2]
//
//b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
//    tile.mqtt.payloadIsJson = state
//    b.tpThermostatPaths.visibility = if (state) VISIBLE else GONE
//}
//
//
//b.tpThermostatTemperatureSub.addTextChangedListener {
//    tile.mqtt.subs["temp"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatTemperatureSetpointSub.addTextChangedListener {
//    tile.mqtt.subs["temp_set"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatTemperatureSetpointPub.addTextChangedListener {
//    tile.mqtt.pubs["temp_set"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatHumiditySub.addTextChangedListener {
//    tile.mqtt.subs["humi"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatHumiditySetpointSub.addTextChangedListener {
//    tile.mqtt.subs["humi_set"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatHumiditySetpointPub.addTextChangedListener {
//    tile.mqtt.pubs["humi_set"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatModeSub.addTextChangedListener {
//    tile.mqtt.subs["mode"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//b.tpThermostatModePub.addTextChangedListener {
//    tile.mqtt.pubs["mode"] = (it ?: "").toString()
//    dashboard.daemon.notifyOptionsChanged()
//}
//
//
//b.tpThermostatTemperatureSetpointPubCopy.setOnClickListener {
//    b.tpThermostatTemperatureSetpointPub.text =
//        b.tpThermostatTemperatureSetpointSub.text
//}
//b.tpThermostatHumiditySetpointPubCopy.setOnClickListener {
//    b.tpThermostatHumiditySetpointPub.text = b.tpThermostatHumiditySetpointSub.text
//}
//b.tpThermostatModePubCopy.setOnClickListener {
//    b.tpThermostatModePub.text = b.tpThermostatModeSub.text
//}
//
//
//b.tpThermostatTemperaturePath.addTextChangedListener {
//    tile.mqtt.jsonPaths["temp"] = (it ?: "").toString()
//}
//b.tpThermostatTemperatureSetpointPath.addTextChangedListener {
//    tile.mqtt.jsonPaths["temp_set"] = (it ?: "").toString()
//}
//b.tpThermostatHumidityPath.addTextChangedListener {
//    tile.mqtt.jsonPaths["humi"] = (it ?: "").toString()
//}
//b.tpThermostatHumiditySetpointPath.addTextChangedListener {
//    tile.mqtt.jsonPaths["humi_set"] = (it ?: "").toString()
//}
//b.tpThermostatModePath.addTextChangedListener {
//    tile.mqtt.jsonPaths["mode"] = (it ?: "").toString()
//}
//
//
//b.tpThermostatTempRetain.setOnCheckedChangeListener { _, isChecked ->
//    tile.retain[0] = isChecked
//}
//b.tpThermostatHumiRetain.setOnCheckedChangeListener { _, isChecked ->
//    tile.retain[1] = isChecked
//}
//b.tpThermostatModeRetain.setOnCheckedChangeListener { _, isChecked ->
//    tile.retain[2] = isChecked
//}
//
//
//b.tpThermostatHumidityStep.addTextChangedListener {
//    tile.humidityStep = it.toString().toFloatOrNull() ?: 5f
//}
//b.tpThermostatTemperatureFrom.addTextChangedListener {
//    tile.temperatureRange[0] = it.toString().toIntOrNull() ?: 15
//}
//b.tpThermostatTemperatureTo.addTextChangedListener {
//    tile.temperatureRange[1] = it.toString().toIntOrNull() ?: 30
//}
//b.tpThermostatTemperatureStep.addTextChangedListener {
//    tile.temperatureStep = it.toString().toFloatOrNull() ?: .5f
//}
//
//
//b.tpThermostatIncludeHumiditySetpoint.setOnCheckedChangeListener { _, state ->
//    tile.includeHumiditySetpoint = state
//    b.tpThermostatHumidityStepBox.visibility = if (state) VISIBLE else GONE
//    b.tpThermostatHumidityTopicsBox.visibility = if (state) VISIBLE else GONE
//}
//b.tpThermostatShowPayload.setOnCheckedChangeListener { _, state ->
//    tile.showPayload = state
//}
//
//setupOptionsRecyclerView(
//tile.modes,
//b.tpThermostatRecyclerView,
//b.tpThermostatModeAdd
//)