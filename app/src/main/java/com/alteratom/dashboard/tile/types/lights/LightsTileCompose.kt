package com.alteratom.tile.types.color.compose

import TilePropComp
import TilePropComp.Communication0
import TilePropComp.Communication1
import TilePropComp.PairList
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.lights.LightsTile

object LightsTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource((tile as LightsTile).iconResFalse), "")
                    }

                    EditText(
                        label = { Text("Off payload") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource((G.tile as LightsTile).iconResTrue), "")
                    }

                    EditText(
                        label = { Text("On payload") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                EditText(
                    label = { Text("State subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("State publish topic") },
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "",
                                tint = Theme.colors.b
                            )
                        }
                    }
                )

                Divider(
                    color = Theme.colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                EditText(
                    label = { Text("Brightness subscribe topic") },
                    value = text,
                    onValueChange = { text = it }
                )

                EditText(
                    label = { Text("Brightness publish topic") },
                    value = text,
                    onValueChange = { text = it },
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "",
                                tint = Theme.colors.b
                            )
                        }
                    }
                )

                Divider(
                    color = Theme.colors.b, thickness = 0.dp, modifier = Modifier
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
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "",
                                tint = Theme.colors.b
                            )
                        }
                    }
                )

                Communication1(retain = false, pointer = {
                    EditText(
                        label = { Text("State JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("Brightness JSON pointer") },
                        value = text,
                        onValueChange = { text = it }
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
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                    )

                    LabeledSwitch(
                        label = {
                            Text(
                                "Include color picker:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                    )

                    Text(
                        "Retain messages:",
                        fontSize = 15.sp,
                        color = Theme.colors.a,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "State",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "Brightness",
                                fontSize = 15.sp,
                                color = Theme.colors.a
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
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }

            val m = (tile as LightsTile).modes
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