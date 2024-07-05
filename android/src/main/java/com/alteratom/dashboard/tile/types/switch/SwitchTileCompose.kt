import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.fragment.TileIconFragment
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconColorPallet
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconHSV
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.getIconRes
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.setIconHSV
import com.alteratom.dashboard.fragment.TileIconFragment.Companion.setIconKey
import com.alteratom.dashboard.compose_daemon.DaemonBasedCompose
import com.alteratom.dashboard.compose_daemon.TilePropertiesComposeComponents
import com.alteratom.dashboard.compose_daemon.TilePropertiesMqttComposeComponents.Communication
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.G.tile

object SwitchTileCompose : DaemonBasedCompose {
    @Composable
    override fun Mqttd(fragment: Fragment) {
        val tile = tile as SwitchTile

        TilePropertiesComposeComponents.CommunicationBox {
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicButton(
                    onClick = {
                        getIconHSV = { tile.hsvFalse }
                        getIconRes = { tile.iconResFalse }
                        getIconColorPallet = { tile.palletFalse }

                        setIconHSV = { hsv -> tile.hsvFalse = hsv }
                        setIconKey = { key -> tile.iconKeyFalse = key }

                        fm.replaceWith(TileIconFragment())
                    },
                    border = BorderStroke(1.dp, tile.palletFalse.cc.color),
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
                BasicButton(
                    onClick = {
                        getIconHSV = { tile.hsvTrue }
                        getIconRes = { tile.iconResTrue }
                        getIconColorPallet = { tile.palletTrue }

                        setIconHSV = { hsv -> tile.hsvTrue = hsv }
                        setIconKey = { key -> tile.iconKeyTrue = key }

                        fm.replaceWith(TileIconFragment())
                    },
                    border = BorderStroke(1.dp, tile.palletTrue.cc.color),
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
        TilePropertiesComposeComponents.Notification(fragment)
    }

    @Composable
    override fun Bluetoothd(fragment: Fragment) {
    }
}