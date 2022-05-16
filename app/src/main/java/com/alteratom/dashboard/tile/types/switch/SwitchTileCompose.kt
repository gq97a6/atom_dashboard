package com.alteratom.tile.types.color.compose

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
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconHSV
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.TileIconFragment
import com.alteratom.dashboard.activities.fragments.tile_properties.MqttTilePropCom.Communication
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropComp
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropType
import com.alteratom.tile.types.switch.SwitchTile

object SwitchTileCompose : TilePropType {
    @Composable
    override fun Mqttd() {
        val tile = tile as SwitchTile
        TilePropComp.Box {
            TilePropComp.CommunicationBox {
                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            getIconHSV = { tile.hsvFalse }
                            getIconRes = { tile.iconResFalse }
                            getIconColorPallet = { tile.palletFalse }

                            setIconHSV = { hsv -> tile.hsvFalse = hsv }
                            setIconKey = { key -> tile.iconKeyFalse = key }

                            fm.replaceWith(TileIconFragment())
                        },
                        border = BorderStroke(0.dp, tile.palletFalse.cc.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(
                            painterResource(tile.iconResFalse),
                            "",
                            tint = tile.palletFalse.cc.color
                        )
                    }

                    var off by remember { mutableStateOf(tile.mqtt.payloads["false"] ?: "") }
                    EditText(
                        label = { Text("Off payload") },
                        value = off,
                        onValueChange = {
                            off = it
                            tile.mqtt.payloads["false"] = it
                        },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = {
                            getIconHSV = { tile.hsvTrue }
                            getIconRes = { tile.iconResTrue }
                            getIconColorPallet = { tile.palletTrue }

                            setIconHSV = { hsv -> tile.hsvTrue = hsv }
                            setIconKey = { key -> tile.iconKeyTrue = key }

                            fm.replaceWith(TileIconFragment())
                        },
                        border = BorderStroke(0.dp, tile.palletTrue.cc.color),
                        modifier = Modifier
                            .height(52.dp)
                            .width(52.dp)
                    ) {
                        Icon(painterResource(tile.iconResTrue), "", tint = tile.palletTrue.cc.color)
                    }

                    var on by remember { mutableStateOf(tile.mqtt.payloads["true"] ?: "") }
                    EditText(
                        label = { Text("On payload") },
                        value = on,
                        onValueChange = {
                            on = it
                            tile.mqtt.payloads["true"] = it
                        },
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }

                Communication()
            }
            TilePropComp.Notification()
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}