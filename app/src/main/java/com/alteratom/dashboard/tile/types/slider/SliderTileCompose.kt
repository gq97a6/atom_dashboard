package com.alteratom.tile.types.color.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.FrameBox
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.fragments.TilePropComp
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