package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.activities.fragments.TilePropComp
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