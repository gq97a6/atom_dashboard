package com.alteratom.tile.types.color.compose

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
import com.alteratom.dashboard.LabeledCheckbox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.TilePropComp
import com.alteratom.dashboard.compose.ComposeObject

object ThermostatTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }
        var index by remember { mutableStateOf(0) }

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

            FrameBox(a = "Type specific: ", b = "color") {
                Column {

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
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}