package com.alteratom.dashboard.activities.fragments.dashboard_properties

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontStyle.Companion.Normal
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose

object DashboardPropertiesCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        FrameBox("Communication:", "MQTT") {
            Column {
                var enabled by remember { mutableStateOf(dashboard.mqtt.isEnabled) }
                LabeledSwitch(
                    label = {
                        Text(
                            "Enabled:",
                            fontSize = 15.sp,
                            color = Theme.colors.a
                        )
                    },
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        dashboard.mqtt.isEnabled = it
                    }
                )

                OutlinedButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(.8f),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = { }
                ) {
                    Text("COPY PROPERTIES", fontSize = 10.sp, color = Theme.colors.a)
                }

                var address by remember { mutableStateOf(dashboard.mqtt.address) }
                EditText(
                    label = { Text("Address") },
                    value = address,
                    onValueChange = {
                        address = it
                        dashboard.mqtt.address = it
                    }
                )

                var port by remember { mutableStateOf(dashboard.mqtt.port.toString()) }
                EditText(
                    label = { Text("Dashboard name") },
                    value = port,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        port = it
                        dashboard.mqtt.port = it.toInt()
                    }
                )

                var id by remember { mutableStateOf(dashboard.mqtt.clientId) }
                EditText(
                    label = { Text("Unique client ID") },
                    value = id,
                    onValueChange = {
                        id = it
                        dashboard.mqtt.clientId = it
                    }
                )

                OutlinedButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(.8f).padding(top = 10.dp),
                    contentPadding = PaddingValues(13.dp),
                    border = BorderStroke(2.dp, Theme.colors.b),
                    onClick = { }
                ) {
                    Text("CONFIGURE SSL", fontSize = 10.sp, color = Theme.colors.a)
                }

                var cred by remember { mutableStateOf(dashboard.mqtt.includeCred) }
                var show by remember { mutableStateOf(false) }
                val rotation = if (show) 0f else 180f

                val angle: Float by animateFloatAsState(
                    targetValue = if (rotation > 360 - rotation) {
                        -(360 - rotation)
                    } else rotation,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )

                Row(
                    Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Include login credentials",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = cred,
                        onCheckedChange = {
                            cred = it
                            show = it
                            dashboard.mqtt.includeCred = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            show = !show
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_arrow), "",
                            tint = Theme.colors.a,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(angle)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = show, enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        var userHidden by remember { mutableStateOf(!dashboard.mqtt.username.isEmpty()) }
                        var user by remember { mutableStateOf(if(userHidden) "hidden" else "") }
                        EditText(
                            label = { Text("User name") },
                            value = user,
                            textStyle = TextStyle(fontStyle = if(userHidden) Italic else Normal),
                            onValueChange = {
                                if(userHidden) {
                                    user = ""
                                    userHidden = false
                                } else user = it
                                dashboard.mqtt.username = user
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    user = ""
                                    userHidden = false
                                    dashboard.mqtt.username = ""
                                }) {
                                    Icon(
                                        painterResource(R.drawable.it_interface_multiply),
                                        "",
                                        tint = Theme.colors.b
                                    )
                                }
                            }
                        )

                        var passHidden by remember { mutableStateOf(!dashboard.mqtt.pass.isEmpty()) }
                        var pass by remember { mutableStateOf(if(passHidden) "hidden" else "") }
                        EditText(
                            label = { Text("Password") },
                            value = pass,
                            textStyle = TextStyle(fontStyle = if(passHidden) Italic else Normal),
                            onValueChange = {
                                if(passHidden) {
                                    pass = ""
                                    passHidden = false
                                } else pass = it
                                dashboard.mqtt.pass = pass
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    pass = ""
                                    passHidden = false
                                    dashboard.mqtt.pass = ""
                                }) {
                                    Icon(
                                        painterResource(R.drawable.it_interface_multiply),
                                        "",
                                        tint = Theme.colors.b
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}