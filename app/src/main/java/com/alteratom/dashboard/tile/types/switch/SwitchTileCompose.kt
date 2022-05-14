package com.alteratom.tile.types.color.compose

import TilePropComp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alteratom.dashboard.EditText
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeObject
import com.alteratom.tile.types.switch.SwitchTile

object SwitchTileCompose : ComposeObject {
    @Composable
    override fun Mqttd() {
        var text by remember { mutableStateOf("false") }

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
                        Icon(painterResource((tile as SwitchTile).iconResFalse), "")
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
                        Icon(painterResource((tile as SwitchTile).iconResTrue), "")
                    }

                    EditText(
                        label = { Text("On payload") },
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                TilePropComp.Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}