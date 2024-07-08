import ColorTile.Companion.ColorTypes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
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
import com.alteratom.dashboard.compose_global.HorizontalRadioGroup
import com.alteratom.dashboard.compose_global.LabeledCheckbox
import com.alteratom.dashboard.compose_global.LabeledSwitch

object ColorTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = aps.tile as ColorTile

        var pub by remember {
            mutableStateOf(
                tile.mqtt.payloads[tile.colorType.toString()] ?: ""
            )
        }
        var type by remember { mutableStateOf(tile.colorType) }


        TilePropertiesComposeComponents.CommunicationBox {
            Communication0()

            EditText(
                label = { Text("Publish payload") },
                value = pub,
                onValueChange = {
                    pub = it
                    tile.mqtt.payloads[type.name] = it
                    tile.colorType = tile.colorType
                }
            )
            Text(
                "Use ${
                    when (type) {
                        ColorTypes.HSV -> "@h, @s, @v"
                        ColorTypes.HEX -> "@hex"
                        ColorTypes.RGB -> "@r, @g, @b"
                    }
                } to insert current value.",
                fontSize = 13.sp,
                color = Theme.colors.a
            )

            Communication1()
        }

        TilePropertiesComposeComponents.Notification(fragment)

        FrameBox(a = "Type specific: ", b = "color") {
            Column {

                var paint by remember { mutableStateOf(tile.doPaint) }
                LabeledSwitch(
                    label = {
                        Text(
                            "Paint tile:",
                            fontSize = 15.sp,
                            color = Theme.colors.a
                        )
                    },
                    checked = paint,
                    onCheckedChange = {
                        paint = it
                        tile.doPaint = it
                    },
                )

                var raw by remember { mutableStateOf(tile.paintRaw) }
                AnimatedVisibility(
                    visible = paint, enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        LabeledCheckbox(
                            label = {
                                Text(
                                    "Paint with raw color (ignore contrast)",
                                    fontSize = 15.sp,
                                    color = Theme.colors.a
                                )
                            },
                            checked = raw,
                            onCheckedChange = {
                                raw = it
                                tile.paintRaw = it
                            },
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }

                val list = listOf(
                    ColorTypes.HSV.name,
                    ColorTypes.HEX.name,
                    ColorTypes.RGB.name
                )

                HorizontalRadioGroup(
                    list,
                    "Type:",
                    list.indexOf(type.name),
                    {
                        type = ColorTypes.values()[it]
                        tile.colorType = ColorTypes.values()[it]
                        pub = tile.mqtt.payloads[tile.colorType.name] ?: ""
                    },
                )
            }
        }
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}