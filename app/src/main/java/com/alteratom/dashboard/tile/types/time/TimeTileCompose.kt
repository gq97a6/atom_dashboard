import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesCompose
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication0
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose.Communication1
import com.alteratom.dashboard.compose.EditText
import com.alteratom.dashboard.compose.FrameBox
import com.alteratom.dashboard.compose.HorizontalRadioGroup
import com.alteratom.dashboard.compose.LabeledSwitch
import com.alteratom.dashboard.foreground_service.demons.DaemonBasedCompose

object TimeTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as TimeTile

        var type by remember { mutableStateOf(if (tile.isDate) 1 else 0) }

        var pub by remember {
            mutableStateOf(
                tile.mqttData.payloads[if (type == 0) "time" else "date"] ?: ""
            )
        }

        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Communication0()

                EditText(
                    label = { Text("Publish payload") },
                    value = pub,
                    onValueChange = {
                        pub = it
                        tile.mqttData.payloads[if (type == 0) "time" else "date"] = it
                    }
                )
                Text(
                    if (type == 0) "Use @hour and @minute to insert current values" else "Use @day, @month, @year to insert current values.",
                    fontSize = 13.sp,
                    color = Theme.colors.a
                )

                Communication1()
            }

            TilePropertiesCompose.Notification()

            FrameBox(a = "Type specific: ", b = "time") {
                Column {

                    AnimatedVisibility(
                        visible = type == 0, enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            var military by remember { mutableStateOf(tile.isMilitary) }
                            LabeledSwitch(
                                label = {
                                    Text(
                                        "24-hour clock:",
                                        fontSize = 15.sp,
                                        color = Theme.colors.a
                                    )
                                },
                                checked = military,
                                onCheckedChange = {
                                    military = it
                                    tile.isMilitary = it
                                },
                            )
                        }
                    }

                    HorizontalRadioGroup(
                        listOf(
                            "Time",
                            "Date",
                        ),
                        "Payload type:",
                        type,
                        {
                            type = it
                            tile.isDate = (it == 1)
                            pub = tile.mqttData.payloads[if (type == 0) "time" else "date"] ?: ""
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