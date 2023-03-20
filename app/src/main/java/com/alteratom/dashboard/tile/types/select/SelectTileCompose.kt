import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose_daemon.TilePropertiesCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesCompose.PairList
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttCompose.Communication
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose

object SelectTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd() {
        val tile = tile as SelectTile

        TilePropertiesCompose.Box {
            TilePropertiesCompose.CommunicationBox {
                Communication()
            }

            TilePropertiesCompose.Notification()

            FrameBox(a = "Type specific: ", b = "select") {
                Row {

                    var show by remember { mutableStateOf(tile.showPayload) }
                    LabeledSwitch(
                        label = {
                            Text(
                                "Show payload on list:",
                                fontSize = 15.sp,
                                color = colors.a
                            )
                        },
                        checked = show,
                        onCheckedChange = {
                            show = it
                            tile.showPayload = it
                        }
                    )
                }
            }

            val o = tile.options
            PairList(
                o,
                { o.removeAt(it) },
                { o.add(Pair("", "")) },
                { i, v -> o[i] = o[i].copy(first = v) },
                { i, v -> o[i] = o[i].copy(second = v) },
            )
        }
    }

    @Composable
    override fun Bluetoothd() {
    }
}