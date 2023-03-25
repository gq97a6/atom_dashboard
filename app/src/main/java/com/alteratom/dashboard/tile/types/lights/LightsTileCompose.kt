import ColorTile.Companion.ColorTypes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.fragments.TileIconFragment
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconColorPallet
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconHSV
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.getIconRes
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.setIconHSV
import com.alteratom.dashboard.activities.fragments.TileIconFragment.Companion.setIconKey
import com.alteratom.dashboard.compose_daemon.TilePropertiesCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesCompose.PairList
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.compose_global.*
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose

object LightsTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as LightsTile

        var type by remember { mutableStateOf(tile.colorType) }
        var pub by remember {
            mutableStateOf(
                tile.mqtt.payloads[tile.colorType.toString()] ?: ""
            )
        }

        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    BasicButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            getIconHSV = { tile.hsvTrue }
                            getIconRes = { tile.iconResTrue }
                            getIconColorPallet = { tile.palletFalse }

                            setIconHSV = { hsv -> tile.hsvTrue = hsv }
                            setIconKey = { key -> tile.iconKeyTrue = key }

                            MainActivity.fm.replaceWith(TileIconFragment())
                        },
                        border = BorderStroke(1.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(
                            painterResource(tile.iconResFalse),
                            "",
                            tint = tile.palletFalse.cc.color
                        )
                    }

                    var off by remember { mutableStateOf(tile.mqtt.payloads["false"] ?: "") }
                    EditText(
                        label = { Text("Off payload") },
                        value = off,
                        onValueChange = {
                            off = it
                            tile.mqtt.payloads["false"] = it
                        },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    BasicButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            getIconHSV = { tile.hsvTrue }
                            getIconRes = { tile.iconResTrue }
                            getIconColorPallet = { tile.palletTrue }

                            setIconHSV = { hsv -> tile.hsvTrue = hsv }
                            setIconKey = { key -> tile.iconKeyTrue = key }

                            MainActivity.fm.replaceWith(TileIconFragment())
                        },
                        border = BorderStroke(1.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource(tile.iconResTrue), "", tint = tile.palletTrue.cc.color)
                    }

                    var on by remember { mutableStateOf(tile.mqtt.payloads["true"] ?: "") }
                    EditText(
                        label = { Text("On payload") },
                        value = on,
                        onValueChange = {
                            on = it
                            tile.mqtt.payloads["true"] = it
                        },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                EditText(
                    label = { Text("Color publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqtt.payloads[type.toString()] = it
                        tile.colorType = tile.colorType
                    }
                )
                Text(
                    "Use ${
                        when (type) {
                            ColorTypes.HSV -> "@h, @s, @v"
                            ColorTypes.HEX -> "@hex"
                            ColorTypes.RGB -> "@r, @g, @b"
                        }
                    } to insert current value.",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Divider(
                    color = Theme.colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

                var stateSub by remember { mutableStateOf(tile.mqtt.subs["state"] ?: "") }
                EditText(
                    label = { Text("State subscribe topic") },
                    value = stateSub,
                    onValueChange = {
                        stateSub = it
                        tile.mqtt.subs["state"] = it
                        G.dashboard.daemon.notifyConfigChanged()
                    }
                )

                var statePub by remember { mutableStateOf(tile.mqtt.pubs["state"] ?: "") }
                EditText(
                    label = { Text("State publish topic") },
                    value = statePub,
                    onValueChange = {
                        statePub = it
                        tile.mqtt.pubs["state"] = it
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            statePub = stateSub
                            tile.mqtt.pubs["state"] = stateSub
                        }) {
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

                var brightSub by remember { mutableStateOf(tile.mqtt.subs["bright"] ?: "") }
                EditText(
                    label = { Text("Brightness subscribe topic") },
                    value = brightSub,
                    onValueChange = {
                        brightSub = it
                        tile.mqtt.subs["bright"] = it
                        G.dashboard.daemon.notifyConfigChanged()
                    }
                )

                var brightPub by remember { mutableStateOf(tile.mqtt.pubs["bright"] ?: "") }
                EditText(
                    label = { Text("Brightness publish topic") },
                    value = brightPub,
                    onValueChange = {
                        brightPub = it
                        tile.mqtt.pubs["bright"] = it
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            brightPub = brightSub
                            tile.mqtt.pubs["bright"] = brightSub
                        }) {
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

                var colorSub by remember { mutableStateOf(tile.mqtt.subs["color"] ?: "") }
                EditText(
                    label = { Text("Color subscribe topic") },
                    value = colorSub,
                    onValueChange = {
                        colorSub = it
                        tile.mqtt.subs["color"] = it
                        G.dashboard.daemon.notifyConfigChanged()
                    }
                )

                var colorPub by remember { mutableStateOf(tile.mqtt.pubs["color"] ?: "") }
                EditText(
                    label = { Text("Color publish topic") },
                    value = colorPub,
                    onValueChange = {
                        colorPub = it
                        tile.mqtt.pubs["color"] = it
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            colorPub = colorSub
                            tile.mqtt.pubs["color"] = colorSub
                        }) {
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

                var modeSub by remember { mutableStateOf(tile.mqtt.subs["mode"] ?: "") }
                EditText(
                    label = { Text("Mode subscribe topic") },
                    value = modeSub,
                    onValueChange = {
                        modeSub = it
                        tile.mqtt.subs["mode"] = it
                        G.dashboard.daemon.notifyConfigChanged()
                    }
                )

                var modePub by remember { mutableStateOf(tile.mqtt.pubs["mode"] ?: "") }
                EditText(
                    label = { Text("Mode publish topic") },
                    value = modePub,
                    onValueChange = {
                        modePub = it
                        tile.mqtt.pubs["mode"] = it
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            modePub = modeSub
                            tile.mqtt.pubs["mode"] = modeSub
                        }) {
                            Icon(
                                painterResource(R.drawable.il_file_copy),
                                "",
                                tint = Theme.colors.b
                            )
                        }
                    }
                )

                Communication1(retain = false, pointer = {
                    var stateJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["state"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("State JSON pointer") },
                        value = stateJson,
                        onValueChange = {
                            stateJson = it
                            tile.mqtt.jsonPaths["state"] = it
                        }
                    )

                    var brightJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["bright"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Brightness JSON pointer") },
                        value = brightJson,
                        onValueChange = {
                            brightJson = it
                            tile.mqtt.jsonPaths["bright"] = it
                        }
                    )

                    var colorJson by remember {
                        mutableStateOf(
                            tile.mqtt.jsonPaths["color"] ?: ""
                        )
                    }
                    EditText(
                        label = { Text("Color JSON pointer") },
                        value = colorJson,
                        onValueChange = {
                            colorJson = it
                            tile.mqtt.jsonPaths["color"] = it
                        }
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

            TilePropertiesCompose.Notification()

            FrameBox(a = "Type specific: ", b = "lights") {
                Column {
                    var show by remember { mutableStateOf(tile.showPayload) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = show,
                        onCheckedChange = {
                            show = it
                            tile.showPayload = it
                        },
                    )

                    var incColor by remember { mutableStateOf(tile.includePicker) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Include color picker:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = incColor,
                        onCheckedChange = {
                            incColor = it
                            tile.includePicker = it
                        },
                    )

                    val list = listOf(
                        ColorTypes.HSV.name,
                        ColorTypes.HEX.name,
                        ColorTypes.RGB.name
                    )

                    HorizontalRadioGroup(
                        list,
                        "Type:",
                        list.indexOf(type.name),
                        {
                            type = ColorTypes.values()[it]
                            tile.colorType = ColorTypes.values()[it]
                            pub = tile.mqtt.payloads[tile.colorType.name] ?: ""
                        },
                    )

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

                    Text(
                        "Retain messages:",
                        fontSize = 15.sp,
                        color = Theme.colors.a,
                        modifier = Modifier.padding(top = 10.dp)
                    )

                    var stateRet by remember { mutableStateOf(tile.retain[0]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "State",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = stateRet,
                        onCheckedChange = {
                            stateRet = it
                            tile.retain[0] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    var brightRet by remember { mutableStateOf(tile.retain[1]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Brightness",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = brightRet,
                        onCheckedChange = {
                            brightRet = it
                            tile.retain[1] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    var colorRet by remember { mutableStateOf(tile.retain[2]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Color",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = colorRet,
                        onCheckedChange = {
                            colorRet = it
                            tile.retain[2] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    var modeRet by remember { mutableStateOf(tile.retain[3]) }
                    LabeledCheckbox(
                        label = {
                            Text(
                                "Mode",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = modeRet,
                        onCheckedChange = {
                            modeRet = it
                            tile.retain[3] = it
                        },
                        modifier = Modifier.padding(vertical = 10.dp)
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