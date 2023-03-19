import ColorTile.Companion.ColorTypes
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompose
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.compose.*
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose

object ColorTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as ColorTile

        var pub by remember {
            mutableStateOf(
                tile.mqttData.payloads[tile.colorType.toString()] ?: ""
            )
        }
        var type by remember { mutableStateOf(tile.colorType) }

        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqttData.payloads[type.name] = it
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

            TilePropertiesCompose.Notification()

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
                            pub = tile.mqttData.payloads[tile.colorType.name] ?: ""
                        },
                    )
                }
            }
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}