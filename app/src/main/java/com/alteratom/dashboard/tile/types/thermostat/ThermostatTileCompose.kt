package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alteratom.R
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.TilePropComp
import com.alteratom.dashboard.compose.ComposeObject

object ThermostatTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }

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

                Divider(color = colors.b, thickness = 0.dp, modifier = Modifier.padding(top = 10.dp).padding(vertical = 10.dp))

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

                Divider(color = colors.b, thickness = 0.dp, modifier = Modifier.padding(top = 10.dp).padding(vertical = 10.dp))

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

                    Divider(color = colors.b, thickness = 0.dp, modifier = Modifier.padding(top = 10.dp).padding(vertical = 10.dp))

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

                    Divider(color = colors.b, thickness = 0.dp, modifier = Modifier.padding(top = 10.dp).padding(vertical = 10.dp))

                    EditText(
                        label = { Text("Mode JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                })
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}