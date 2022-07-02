package com.alteratom.dashboard.activities.fragments.tile_properties

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.*

object TilePropertiesMqttCompose {

    @Composable
    fun Communication0() {

        var sub by remember { mutableStateOf(G.tile.mqttData.subs["base"] ?: "") }
        EditText(
            label = { Text("Subscribe topic") },
            value = sub,
            onValueChange = {
                sub = it
                G.tile.mqttData.subs["base"] = it
                G.dashboard.daemon.notifyOptionsChanged()
            }
        )

        var pub by remember { mutableStateOf(G.tile.mqttData.pubs["base"] ?: "") }
        EditText(
            label = { Text("Publish topic") },
            value = pub,
            onValueChange = {
                pub = it
                G.tile.mqttData.pubs["base"] = it
                G.dashboard.daemon.notifyOptionsChanged()
            },
            trailingIcon = {
                IconButton(onClick = {
                    pub = sub
                    G.tile.mqttData.pubs["base"] = sub
                    G.dashboard.daemon.notifyOptionsChanged()
                }) {
                    Icon(painterResource(R.drawable.il_file_copy), "", tint = Theme.colors.b)
                }
            }
        )
    }

    @Composable
    fun Communication1(
        retain: Boolean = true, pointer: @Composable () -> Unit = {
            var json by remember { mutableStateOf(G.tile.mqttData.jsonPaths["base"] ?: "") }
            EditText(
                label = { Text("Payload JSON pointer") },
                value = json,
                onValueChange = {
                    json = it
                    G.tile.mqttData.jsonPaths["base"] = it
                }
            )
        }
    ) {

        var qos by remember { mutableStateOf(G.tile.mqttData.qos) }
        RadioGroup(
            listOf(
                "QoS 0: At most once. No guarantee.",
                "QoS 1: At least once. (Recommended)",
                "QoS 2: Delivery exactly once."
            ), "Quality of Service (MQTT protocol):",
            qos,
            {
                qos = it
                G.tile.mqttData.qos = it
                G.dashboard.daemon.notifyOptionsChanged()
            },
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        if (retain) {
            var ret by remember { mutableStateOf(G.tile.mqttData.doRetain) }
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = Theme.colors.a) },
                checked = ret,
                onCheckedChange = {
                    ret = it
                    G.tile.mqttData.doRetain = it
                }
            )
        }

        var conf by remember { mutableStateOf(G.tile.mqttData.doConfirmPub) }
        LabeledSwitch(
            label = { Text("Confirm publishing:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = conf,
            onCheckedChange = {
                conf = it
                G.tile.mqttData.doConfirmPub = it
            }
        )

        var json by remember { mutableStateOf(G.tile.mqttData.payloadIsJson) }
        LabeledSwitch(
            label = { Text("Payload is JSON:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = json,
            onCheckedChange = {
                json = it
                G.tile.mqttData.payloadIsJson = it
            }
        )

        AnimatedVisibility(
            visible = json, enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                pointer()
            }
        }
    }

    @Composable
    fun Communication() {
        Communication0()
        Communication1()
    }
}