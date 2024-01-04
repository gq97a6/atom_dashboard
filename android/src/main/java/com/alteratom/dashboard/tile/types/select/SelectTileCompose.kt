import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents.PairList
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.`object`.G.tile

object SelectTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = tile as SelectTile


        TilePropertiesComposeComponents.CommunicationBox {
            Communication()
        }

        TilePropertiesComposeComponents.Notification(fragment)

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

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}