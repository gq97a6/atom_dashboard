package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.compose.ComposeObject

object TextTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var index by remember { mutableStateOf(0) }
        var state by remember { mutableStateOf(true) }
        var text by remember { mutableStateOf("false") }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = text,
                    onValueChange = { text = it }
                )

                RadioGroup(
                    listOf(
                        "Variable (set on send)",
                        "Static (always the same)",
                    ), "Payload setting type",
                    index,
                    { index = it },
                    modifier = Modifier.padding(top = 20.dp)
                )

                TilePropComp.Communication1()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "text") {
                Row {
                    LabeledSwitch(
                        label = { Text("Full width:", fontSize = 15.sp, color = Theme.colors.a) },
                        checked = state,
                        onCheckedChange = { state = it }
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

                val tile = tile as TextTile

                b.tpText.visibility = VISIBLE
                b.tpMqttPayloadTypeBox.visibility = VISIBLE

                b.tpTextBig.isChecked = tile.isBig

                b.tpTextBig.setOnCheckedChangeListener { _, isChecked ->
                    tile.isBig = isChecked
                }

                b.tpPayloadType.check(
                    if (tile.mqtt.varPayload) R.id.tp_mqtt_payload_var
                    else {
                        b.tpMqttPayload.visibility = VISIBLE
                        R.id.tp_mqtt_payload_val
                    }
                )
                b.tpPayloadType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.mqtt.varPayload = when (id) {
                        R.id.tp_mqtt_payload_val -> {
                            b.tpMqttPayloadBox.visibility = VISIBLE
                            false
                        }
                        R.id.tp_mqtt_payload_var -> {
                            b.tpMqttPayloadBox.visibility = GONE
                            true
                        }
                        else -> true
                    }
                }

                b.tpMqttJsonPayloadPath.addTextChangedListener {
                    tile.mqtt.jsonPaths["base"] = (it ?: "").toString()
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads["base"] = (it ?: "").toString()
                }
 */