package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.compose.ComposeObject

object TimeTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var index by remember { mutableStateOf(0) }
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = text,
                    onValueChange = { text = it }
                )
                Text(
                    "Use @hour and @minute to insert current values",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                TilePropComp.Communication1()
            }

            TilePropComp.Notification()

            FrameBox(a = "Type specific: ", b = "time") {
                Column {

                    LabeledSwitch(
                        label = {
                            Text(
                                "24-hour clock:",
                                fontSize = 15.sp,
                                color = Theme.colors.a
                            )
                        },
                        checked = state,
                        onCheckedChange = { state = it },
                    )

                    HorizontalRadioGroup(
                        listOf(
                            "Time",
                            "Date",
                        ), "Payload type:",
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

/*

                val tile = tile as TimeTile

                b.tpTime.visibility = VISIBLE
                b.tpMqttPayloadBox.visibility = VISIBLE
                b.tpMqttPayloadHint.visibility = VISIBLE


                b.tpTimeType.check(
                    when (tile.isDate) {
                        false -> {
                            b.tpMqttPayload.setText(tile.mqtt.payloads["time"])
                            b.tpMqttPayloadHint.text =
                                "Use @hour and @minute to insert current values."
                            R.id.tp_time_time
                        }
                        true -> {
                            b.tpMqttPayload.setText(tile.mqtt.payloads["date"])
                            b.tpMqttPayloadHint.text =
                                "Use @day, @month, @year to insert current values."
                            R.id.tp_time_date
                        }
                    }
                )

                if (!tile.isDate) {
                    b.tpTimeMilitaryBox.visibility = VISIBLE
                    b.tpTimeMilitary.isChecked = tile.isMilitary
                }

                b.tpTimeType.setOnCheckedChangeListener { _: RadioGroup, id: Int ->
                    tile.isDate = when (id) {
                        R.id.tp_time_time -> false
                        R.id.tp_time_date -> true
                        else -> false
                    }

                    b.tpTimeMilitaryBox.visibility = if (tile.isDate) GONE else VISIBLE
                    b.tpMqttPayload.setText(tile.mqtt.payloads[if (tile.isDate) "date" else "time"])
                    b.tpMqttPayloadHint.text =
                        "Use ${if (tile.isDate) "@day, @month, @year" else "@hour and @minute"} to insert current values."
                }

                if (!tile.isDate) {
                    b.tpTimeMilitary.setOnCheckedChangeListener { _, state ->
                        tile.isMilitary = state
                    }
                }

                b.tpMqttPayload.addTextChangedListener {
                    tile.mqtt.payloads[if (tile.isDate) "date" else "time"] =
                        (it ?: "").toString()
                }
 */