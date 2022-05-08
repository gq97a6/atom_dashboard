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

object ColorTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }
        var state by remember { mutableStateOf(true) }
        var index by remember { mutableStateOf(0) }

        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                TilePropComp.Communication0()

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

                TilePropComp.Communication1()
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