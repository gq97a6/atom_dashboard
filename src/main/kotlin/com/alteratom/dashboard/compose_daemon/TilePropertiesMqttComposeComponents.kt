package com.alteratom.dashboard.compose_daemon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.R
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.RadioGroup

object TilePropertiesMqttComposeComponents {

    @Composable
    fun Communication0() {

        var sub by remember { mutableStateOf(aps.tile.mqtt.subs["base"] ?: "") }
        EditText(
            label = { Text("Subscribe topic") },
            value = sub,
            onValueChange = {
                sub = it
                aps.tile.mqtt.subs["base"] = it
                aps.dashboard.daemon?.notifyConfigChanged()
            }
        )

        var pub by remember { mutableStateOf(aps.tile.mqtt.pubs["base"] ?: "") }
        EditText(
            label = { Text("Publish topic") },
            value = pub,
            onValueChange = {
                pub = it
                aps.tile.mqtt.pubs["base"] = it
                aps.dashboard.daemon?.notifyConfigChanged()
            },
            trailingIcon = {
                IconButton(onClick = {
                    pub = sub
                    aps.tile.mqtt.pubs["base"] = sub
                    aps.dashboard.daemon?.notifyConfigChanged()
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
            var json by remember { mutableStateOf(aps.tile.mqtt.jsonPaths["base"] ?: "") }
            EditText(
                label = { Text("Payload JSON pointer") },
                value = json,
                onValueChange = {
                    json = it
                    aps.tile.mqtt.jsonPaths["base"] = it
                }
            )
        }
    ) {

        var qos by remember { mutableIntStateOf(aps.tile.mqtt.qos) }
        RadioGroup(
            listOf(
                "QoS 0: At most once. No guarantee.",
                "QoS 1: At least once. (Recommended)",
                "QoS 2: Delivery exactly once."
            ), "Quality of Service (MQTT protocol):",
            qos,
            {
                qos = it
                aps.tile.mqtt.qos = it
                aps.dashboard.daemon?.notifyConfigChanged()
            },
            modifier = Modifier.padding(top = 20.dp, bottom = 10.dp)
        )

        if (retain) {
            var ret by remember { mutableStateOf(aps.tile.mqtt.doRetain) }
            LabeledSwitch(
                label = { Text("Retain massages:", fontSize = 15.sp, color = Theme.colors.a) },
                checked = ret,
                onCheckedChange = {
                    ret = it
                    aps.tile.mqtt.doRetain = it
                }
            )
        }

        var conf by remember { mutableStateOf(aps.tile.mqtt.doConfirmPub) }
        LabeledSwitch(
            label = { Text("Confirm publishing:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = conf,
            onCheckedChange = {
                conf = it
                aps.tile.mqtt.doConfirmPub = it
            }
        )

        var json by remember { mutableStateOf(aps.tile.mqtt.payloadIsJson) }
        LabeledSwitch(
            label = { Text("Payload is JSON:", fontSize = 15.sp, color = Theme.colors.a) },
            checked = json,
            onCheckedChange = {
                json = it
                aps.tile.mqtt.payloadIsJson = it
            }
        )

        AnimatedVisibility(
            visible = json, enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                pointer()
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    @Composable
    fun Communication() {
        Communication0()
        Communication1()
    }
}