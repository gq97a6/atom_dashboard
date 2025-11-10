import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication0
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication1
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.RadioGroup

object TextTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = aps.tile as TextTile

        TilePropertiesComposeComponents.CommunicationBox {
            Communication0()

            var pub by remember { mutableStateOf(tile.mqtt.payloads["base"] ?: "") }
            var type by remember { mutableStateOf(if (tile.mqtt.payloadIsVar) 0 else 1) }

            AnimatedVisibility(
                visible = type == 1, enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    EditText(
                        label = { Text("Publish payload") },
                        value = pub,
                        onValueChange = {
                            pub = it
                            tile.mqtt.payloads["base"] = it
                        }
                    )
                }
            }

            RadioGroup(
                listOf(
                    "Variable (set on send)",
                    "Static (always the same)",
                ), "Payload setting type",
                type,
                {
                    type = it
                    tile.mqtt.payloadIsVar = it == 0
                },
                modifier = Modifier.padding(top = 20.dp)
            )

            Communication1()
        }

        TilePropertiesComposeComponents.Notification(fragment)

        FrameBox(a = "Type specific: ", b = "text") {
            Row {
                var big by remember { mutableStateOf(tile.isBig) }
                LabeledSwitch(
                    label = { Text("Full width:", fontSize = 15.sp, color = Theme.colors.a) },
                    checked = big,
                    onCheckedChange = {
                        big = it
                        tile.isBig = it
                    }
                )
            }
        }
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}