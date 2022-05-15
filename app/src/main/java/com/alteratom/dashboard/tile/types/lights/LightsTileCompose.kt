package com.alteratom.tile.types.color.compose

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
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication1
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp.PairList
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.lights.LightsTile

object LightsTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        val tile = tile as LightsTile

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
                        Icon(painterResource(tile.iconResFalse), "")
                    }
                    tile.colorType
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
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {},
                        border = BorderStroke(0.dp, Theme.colors.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource(tile.iconResTrue), "")
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

                var brightSub by remember { mutableStateOf(tile.mqtt.subs["bright"] ?: "") }
                EditText(
                    label = { Text("Brightness subscribe topic") },
                    value = brightSub,
                    onValueChange = {
                        brightSub = it
                        tile.mqtt.subs["bright"] = it
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

                var colorSub by remember { mutableStateOf(tile.mqtt.subs["color"] ?: "") }
                EditText(
                    label = { Text("Brightness subscribe topic") },
                    value = colorSub,
                    onValueChange = {
                        colorSub = it
                        tile.mqtt.subs["color"] = it
                    }
                )

                var colorPub by remember { mutableStateOf(tile.mqtt.pubs["color"] ?: "") }
                EditText(
                    label = { Text("Brightness publish topic") },
                    value = colorPub,
                    onValueChange = {
                        colorPub = it
                        tile.mqtt.pubs["color"] = it
                    },
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

                var modeSub by remember { mutableStateOf(tile.mqtt.subs["mode"] ?: "") }
                EditText(
                    label = { Text("Mode subscribe topic") },
                    value = modeSub,
                    onValueChange = {
                        modeSub = it
                        tile.mqtt.subs["mode"] = it
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
                    var stateJson by remember { mutableStateOf(tile.mqtt.jsonPaths["state"] ?: "") }
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

                    var modeJson by remember { mutableStateOf(tile.mqtt.jsonPaths["mode"] ?: "") }
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

            TilePropComp.Notification()

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

                    var type by remember { mutableStateOf(0) }
                    HorizontalRadioGroup(
                        listOf(
                            "HSV",
                            "HEX",
                            "RGB",
                        ),
                        "Type:",
                        type,
                        {
                            type = it
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
                        onCheckedChange = { paint = it },
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
                        onCheckedChange = { stateRet = it
                            tile.retain[0] = it},
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
                        onCheckedChange = { brightRet = it },
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
                        onCheckedChange = { colorRet = it },
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
                        onCheckedChange = { modeRet = it },
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
/*

                b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])

                b.tpMqttPayloadTag.text = "Color publish payload"
                b.tpMqttPayloadHint.text =
                    "Use ${
                        when (tile.colorType) {
                            "hsv" -> "@h, @s, @v"
                            "hex" -> "@hex"
                            "rgb" -> "@r, @g, @b"
                            else -> "@hex"
                        }
                    } to insert current value."
                b.tpLightsColorType.check(
                    when (tile.colorType) {
                        "hsv" -> R.id.tp_lights_hsv
                        "hex" -> R.id.tp_lights_hex
                        "rgb" -> R.id.tp_lights_rgb
                        else -> R.id.tp_lights_hsv
                    }
                )


                b.tpLightsStatePubCopy.setOnClickListener {
                    b.tpLightsStatePub.text = b.tpLightsStateSub.text
                }
                b.tpLightsColorPubCopy.setOnClickListener {
                    b.tpLightsColorPub.text = b.tpLightsColorSub.text
                }
                b.tpLightsBrightnessPubCopy.setOnClickListener {
                    b.tpLightsBrightnessPub.text = b.tpLightsBrightnessSub.text
                }
                b.tpLightsModePubCopy.setOnClickListener {
                    b.tpLightsModePub.text = b.tpLightsModeSub.text
                }


                b.tpMqttPayloadTrue.addTextChangedListener {
                    tile.mqtt.payloads["true"] = (it ?: "").toString()
                }
                b.tpMqttPayloadFalse.addTextChangedListener {
                    tile.mqtt.payloads["false"] = (it ?: "").toString()
                }
                b.tpMqttPayloadTrueEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvTrue }
                    getIconRes = { tile.iconResTrue }
                    getIconColorPallet = { tile.colorPalletTrue }

                    setIconHSV = { hsv -> tile.hsvTrue = hsv }
                    setIconKey = { key -> tile.iconKeyTrue = key }

                    fm.replaceWith(TileIconFragment())
                }

                b.tpMqttPayloadFalseEditIcon.setOnClickListener {
                    getIconHSV = { tile.hsvFalse }
                    getIconRes = { tile.iconResFalse }
                    getIconColorPallet = { tile.colorPalletFalse }

                    setIconHSV = { hsv -> tile.hsvFalse = hsv }
                    setIconKey = { key -> tile.iconKeyFalse = key }

                    fm.replaceWith(TileIconFragment())
                }


                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads[tile.colorType] = (it ?: "").toString()
                }
                b.tpLightsIncludePicker.setOnCheckedChangeListener { _, state ->
                    tile.includePicker = state
                    (if (tile.includePicker) VISIBLE else GONE).let {
                        b.tpLightsColorRetain.visibility = it
                        b.tpLightsColorTopics.visibility = it
                        b.tpLightsTypeBox.visibility = it
                        b.tpMqttPayloadBox.visibility = it
                        b.tpLightsColorPathBox.visibility = it
                        b.tpLightsPaintBox.visibility = it
                    }
                }
                b.tpLightsDoPaint.setOnCheckedChangeListener { _, state ->
                    tile.doPaint = state
                    b.tpLightsPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
                }

                b.tpLightsPaintRaw.setOnCheckedChangeListener { _, state ->
                    tile.paintRaw = state
                }
                b.tpLightsShowPayload.setOnCheckedChangeListener { _, state ->
                    tile.showPayload = state
                }
                b.tpMqttJsonSwitch.setOnCheckedChangeListener { _, state ->
                    tile.mqtt.payloadIsJson = state
                    b.tpLightsPaths.visibility = if (state) VISIBLE else GONE
                }
                b.tpLightsColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.colorType = when (id) {
                        R.id.tp_lights_hsv -> "hsv"
                        R.id.tp_lights_hex -> "hex"
                        R.id.tp_lights_rgb -> "rgb"
                        else -> "hex"
                    }

                    b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
                    b.tpMqttPayloadHint.text =
                        "Use ${
                            when (tile.colorType) {
                                "hsv" -> "@h, @s, @v"
                                "hex" -> "@hex"
                                "rgb" -> "@r, @g, @b"
                                else -> "@hex"
                            }
                        } to insert current value."
                }


                setupOptionsRecyclerView(tile.modes, b.tpLightsRecyclerView, b.tpLightsAdd)
 */