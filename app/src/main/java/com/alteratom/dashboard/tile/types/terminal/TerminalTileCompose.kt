package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.RadioGroup
import com.alteratom.dashboard.compose.ComposeObject

object TerminalTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var index by remember { mutableStateOf(0) }
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
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}