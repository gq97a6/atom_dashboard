package com.alteratom.dashboard.compose_daemon

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
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.RadioGroup

object TilePropertiesMqttCompose {

    @Composable
    fun Communication0() {

        var sub by remember { mutableStateOf(G.tile.mqtt.subs["base"] ?: "") }
        EditText(
            label = { Text("Subscribe topic") },
            value = sub,
            onValueChange = {
                sub = it
                G.tile.mqtt.subs["base"] = it
                G.dashboard.daemon.notifyConfigChanged()
            }
        )

        var pub by remember { mutableStateOf(G.tile.mqtt.pubs["base"] ?: "") }
        EditText(
            label = { Text("Publish topic") },
            value = pub,
            onValueChange = {
                pub = it
                G.tile.mqtt.pubs["base"] = it
                G.dashboard.daemon.notifyConfigChanged()
            },
            trailingIcon = {
                IconButton(onClick = {
                    pub = sub
                    G.tile.mqtt.pubs["base"] = sub
                    G.dashboard.daemon.notifyConfigChanged()
                }) {
                    Icon(painterResource(R.drawable.il_file_copy), "", tint = Theme.colors.b)
                }
            }
        )
    }

    @Composable
    fun Communication1(
        retain: Boolean = true,
        pointer: @Composable () -> Unit = {
            var json by remember { mutableStateOf(G.tile.mqtt.jsonPaths["base"] ?: "") }
            EditText(
                label = { Text("Payload JSON pointer") },
                value = json,
                onValueChange = {
                    json = it
                    G.tile.mqtt.jsonPaths["base"] = it
                }
            )
        }
    ) {

        var qos by remember { mutableStateOf(G.tile.mqtt.qos) }
        RadioGroup(
            listOf(
                "QoS 0: At most once. No guarantee.",
                "QoS 1: At least once. (Recommended)",
                "QoS 2: Delivery exactly once."
            ), "Quality of Service (MQTT protocol):",
            qos,
            {
                qos = it
                G.tile.mqtt.qos = it
                G.dashboard.daemon.notifyConfigChanged()
            },
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        if (retain) {
            var ret by remember { mutableStateOf(G.tile.mqtt.doRetain) }
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = Theme.colors.a) },
                checked = ret,
                onCheckedChange = {
                    ret = it
                    G.tile.mqtt.doRetain = it
                }
            )
        }

        var conf by remember { mutableStateOf(G.tile.mqtt.doConfirmPub) }
        LabeledSwitch(
            label = { Text("Confirm publishing:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = conf,
            onCheckedChange = {
                conf = it
                G.tile.mqtt.doConfirmPub = it
            }
        )

        var json by remember { mutableStateOf(G.tile.mqtt.payloadIsJson) }
        LabeledSwitch(
            label = { Text("Payload is JSON:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = json,
            onCheckedChange = {
                json = it
                G.tile.mqtt.payloadIsJson = it
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