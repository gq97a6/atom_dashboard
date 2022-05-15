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

                Divider(
                    color = Theme.colors.b, thickness = 0.dp, modifier = Modifier
                        .padding(top = 10.dp)
                        .padding(vertical = 10.dp)
                )

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

            FrameBox(a = "Type specific: ", b = "lights") {
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
/*

                val tile = tile as LightsTile

                setupIcon(
                    tile.iconResFalse,
                    tile.colorPalletFalse.color,
                    b.tpMqttPayloadFalseIconFrame,
                    b.tpMqttPayloadFalseIcon
                )
                setupIcon(
                    tile.iconResTrue,
                    tile.colorPalletTrue.color,
                    b.tpMqttPayloadTrueIconFrame,
                    b.tpMqttPayloadTrueIcon
                )

                (if (tile.includePicker) VISIBLE else GONE).let {
                    b.tpLightsColorRetain.visibility = it
                    b.tpLightsColorTopics.visibility = it
                    b.tpLightsTypeBox.visibility = it
                    b.tpMqttPayloadBox.visibility = it
                    b.tpLightsColorPathBox.visibility = it
                    b.tpLightsPaintBox.visibility = it
                }

                b.tpMqttRetainBox.visibility = GONE
                b.tpMqttTopics.visibility = GONE
                b.tpMqttJsonPayload.visibility = GONE
                b.tpLights.visibility = VISIBLE
                b.tpLightsTopics.visibility = VISIBLE
                b.tpMqttPayloadsBox.visibility = VISIBLE
                b.tpMqttPayloadHint.visibility = VISIBLE
                b.tpLightsPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
                b.tpLightsPaths.visibility =
                    if (tile.mqtt.payloadIsJson) VISIBLE else GONE

                b.tpLightsStateSub.setText(tile.mqtt.subs["state"])
                b.tpLightsStatePub.setText(tile.mqtt.pubs["state"])
                b.tpLightsColorSub.setText(tile.mqtt.subs["color"])
                b.tpLightsColorPub.setText(tile.mqtt.pubs["color"])
                b.tpLightsBrightnessSub.setText(tile.mqtt.subs["bright"])
                b.tpLightsBrightnessPub.setText(tile.mqtt.pubs["bright"])
                b.tpLightsModeSub.setText(tile.mqtt.subs["mode"])
                b.tpLightsModePub.setText(tile.mqtt.pubs["mode"])

                b.tpLightsStatePath.setText(tile.mqtt.jsonPaths["state"])
                b.tpLightsColorPath.setText(tile.mqtt.jsonPaths["color"])
                b.tpLightsBrightnessPath.setText(tile.mqtt.jsonPaths["bright"])
                b.tpLightsModePath.setText(tile.mqtt.jsonPaths["mode"])


                b.tpMqttPayloadFalse.setText(tile.mqtt.payloads["false"] ?: "")
                b.tpMqttPayloadTrue.setText(tile.mqtt.payloads["true"] ?: "")
                b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])

                b.tpLightsDoPaint.isChecked = tile.doPaint
                b.tpLightsPaintRaw.isChecked = tile.paintRaw
                b.tpLightsShowPayload.isChecked = tile.showPayload
                b.tpLightsStateRetain.isChecked = tile.retain[0]
                b.tpLightsColorRetain.isChecked = tile.retain[1]
                b.tpLightsBrightnessRetain.isChecked = tile.retain[2]
                b.tpLightsModeRetain.isChecked = tile.retain[3]
                b.tpLightsIncludePicker.isChecked = tile.includePicker
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

                b.tpLightsStateSub.addTextChangedListener {
                    tile.mqtt.subs["state"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsStatePub.addTextChangedListener {
                    tile.mqtt.pubs["state"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsColorSub.addTextChangedListener {
                    tile.mqtt.subs["color"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsColorPub.addTextChangedListener {
                    tile.mqtt.pubs["color"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsBrightnessSub.addTextChangedListener {
                    tile.mqtt.subs["bright"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsBrightnessPub.addTextChangedListener {
                    tile.mqtt.pubs["bright"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsModeSub.addTextChangedListener {
                    tile.mqtt.subs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }
                b.tpLightsModePub.addTextChangedListener {
                    tile.mqtt.pubs["mode"] = (it ?: "").toString()
                    dashboard.daemon.notifyOptionsChanged()
                }


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


                b.tpLightsStatePath.addTextChangedListener {
                    tile.mqtt.jsonPaths["state"] = (it ?: "").toString()
                }
                b.tpLightsColorPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["color"] = (it ?: "").toString()
                }
                b.tpLightsBrightnessPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["brightness"] = (it ?: "").toString()
                }
                b.tpLightsModePath.addTextChangedListener {
                    tile.mqtt.jsonPaths["mode"] = (it ?: "").toString()
                }


                b.tpLightsStateRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[0] = isChecked
                }
                b.tpLightsColorRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[1] = isChecked
                }
                b.tpLightsBrightnessRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[2] = isChecked
                }
                b.tpLightsModeRetain.setOnCheckedChangeListener { _, isChecked ->
                    tile.retain[3] = isChecked
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