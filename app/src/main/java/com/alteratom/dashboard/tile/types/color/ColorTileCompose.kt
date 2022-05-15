package com.alteratom.tile.types.color.compose

import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication1
import com.alteratom.dashboard.compose.ComposeObject

object ColorTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }
        var index by remember { mutableStateOf(0) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = text,
                    onValueChange = { text = it }
                )
                Text(
                    "Use @hex to insert current value",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Communication1()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "color") {
                Column {

                    LabeledSwitch(
                        label = {
                            Text(
                                "Paint tile:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                    )

                    LabeledCheckbox(
                        label = {
                            Text(
                                "Paint with raw color (ignore contrast)",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    HorizontalRadioGroup(
                        listOf(
                            "HSV",
                            "HEX",
                            "RGB",
                        ),
                        "Type:",
                        index,
                        { index = it },
                    )
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}


//val tile = tile as ColorTile
//
//b.tpColor.visibility = VISIBLE
//b.tpMqttPayloadBox.visibility = VISIBLE
//b.tpMqttPayloadHint.visibility = VISIBLE
//b.tpColorPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
//
//b.tpColorDoPaint.isChecked = tile.doPaint
//b.tpColorPaintRaw.isChecked = tile.paintRaw
//
//b.tpColorColorType.check(
//when (tile.colorType) {
//    "hsv" -> R.id.tp_color_hsv
//    "hex" -> R.id.tp_color_hex
//    "rgb" -> R.id.tp_color_rgb
//    else -> R.id.tp_color_hsv
//}
//)
//
//b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
//b.tpMqttPayloadHint.text =
//"Use ${
//when (tile.colorType) {
//    "hsv" -> "@h, @s, @v"
//    "hex" -> "@hex"
//    "rgb" -> "@r, @g, @b"
//    else -> "@hex"
//}
//} to insert current value."
//
//b.tpColorDoPaint.setOnCheckedChangeListener { _, state ->
//    tile.doPaint = state
//    b.tpColorPaintRawBox.visibility = if (tile.doPaint) VISIBLE else GONE
//}
//
//b.tpColorPaintRaw.setOnCheckedChangeListener { _, state ->
//    tile.paintRaw = state
//}
//
//b.tpColorColorType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
//    tile.colorType = when (id) {
//        R.id.tp_color_hsv -> "hsv"
//        R.id.tp_color_hex -> "hex"
//        R.id.tp_color_rgb -> "rgb"
//        else -> "hex"
//    }
//
//    b.tpMqttPayload.setText(tile.mqtt.payloads[tile.colorType])
//    b.tpMqttPayloadHint.text =
//        "Use ${
//            when (tile.colorType) {
//                "hsv" -> "@h, @s, @v"
//                "hex" -> "@hex"
//                "rgb" -> "@r, @g, @b"
//                else -> "@hex"
//            }
//        } to insert current value."
//}
//
//b.tpMqttPayload.addTextChangedListener {
//    tile.mqtt.payloads[tile.colorType] = (it ?: "").toString()
//}