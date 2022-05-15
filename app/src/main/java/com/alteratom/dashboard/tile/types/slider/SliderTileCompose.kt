package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeObject

object SliderTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = text,
                    onValueChange = { text = it }
                )
                Text(
                    "User @value to insert current value",
                    fontSize = 13.sp,
                    color = colors.a
                )

                TilePropComp.Communication1()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "slider") {
                Column {
                    var state by remember { mutableStateOf(true) }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LabeledSwitch(
                            label = { Text("Drag to control:", fontSize = 15.sp, color = colors.a) },
                            checked = state,
                            onCheckedChange = { state = it }
                        )

                        Text(
                            "EXPERIMENTAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.a,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    EditText(
                        label = { Text("From value") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("To value") },
                        value = text,
                        onValueChange = { text = it }
                    )

                    EditText(
                        label = { Text("Step value") },
                        value = text,
                        onValueChange = { text = it }
                    )
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}
/*

                val tile = tile as SliderTile

                b.tpSlider.visibility = VISIBLE
                b.tpMqttPayloadBox.visibility = VISIBLE
                b.tpMqttPayloadHint.visibility = VISIBLE

                b.tpSliderDrag.isChecked = tile.dragCon

                b.tpMqttPayloadHint.text = "Use @value to insert current value"
                b.tpSliderFrom.setText(tile.range[0].toString())
                b.tpSliderTo.setText(tile.range[1].toString())
                b.tpSliderStep.setText(tile.range[2].toString())

                b.tpSliderFrom.addTextChangedListener { it ->
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderFrom.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[0] = it
                            }
                        }
                    }
                }

                b.tpSliderTo.addTextChangedListener { it ->
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderTo.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[1] = it
                            }
                        }
                    }
                }

                b.tpSliderStep.addTextChangedListener { it ->
                    (it ?: "").toString().let { raw ->
                        (it ?: "").toString().digitsOnly().let { parsed ->
                            if (raw != parsed) b.tpSliderStep.setText(parsed)
                            else parsed.toIntOrNull()?.let {
                                tile.range[2] = it
                            }
                        }
                    }
                }

                b.tpSliderDrag.setOnCheckedChangeListener { _, state ->
                    tile.dragCon = state
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads["base"] = (it ?: "").toString()
                }
 */