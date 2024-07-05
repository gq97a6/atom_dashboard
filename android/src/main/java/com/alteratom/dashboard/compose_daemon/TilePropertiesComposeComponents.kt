package com.alteratom.dashboard.compose_daemon

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.fragment.TileIconFragment
import com.alteratom.dashboard.activity.fragment.TileIconFragment.Companion.getIconColorPallet
import com.alteratom.dashboard.activity.fragment.TileIconFragment.Companion.getIconHSV
import com.alteratom.dashboard.activity.fragment.TileIconFragment.Companion.getIconRes
import com.alteratom.dashboard.activity.fragment.TileIconFragment.Companion.setIconHSV
import com.alteratom.dashboard.activity.fragment.TileIconFragment.Companion.setIconKey
import com.alteratom.dashboard.areNotificationsAllowed
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.BoldStartText
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledCheckbox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.NavigationArrows
import com.alteratom.dashboard.compose_global.nrClickable
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.G.dashboard
import com.alteratom.dashboard.helper_objects.G.settings
import com.alteratom.dashboard.helper_objects.G.tile
import com.alteratom.dashboard.requestNotifications
import com.alteratom.dashboard.switcher.TileSwitcher
import java.util.Locale

object TilePropertiesComposeComponents {
    @Composable
    inline fun Box(crossinline content: @Composable () -> Unit) {

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(text = "Tile properties", fontSize = 45.sp, color = Theme.colors.color)
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicButton(
                    contentPadding = PaddingValues(13.dp),
                    onClick = {
                        getIconHSV = { tile.hsv }
                        getIconRes = { tile.iconRes }
                        getIconColorPallet = { tile.pallet }

                        setIconHSV = { hsv -> tile.hsv = hsv }
                        setIconKey = { key -> tile.iconKey = key }

                        fm.replaceWith(TileIconFragment())
                    },
                    border = BorderStroke(1.dp, tile.pallet.cc.color),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(painterResource(tile.iconRes), "", tint = tile.pallet.cc.color)
                }

                val typeTag = tile.typeTag.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }

                var tag by remember { mutableStateOf(tile.tag) }
                EditText(
                    label = { BoldStartText("$typeTag ", "tile tag") },
                    value = tag,
                    onValueChange = {
                        tag = it
                        tile.tag = it
                    },
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            content()

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            )
        }

        if (!settings.hideNav && dashboard.tiles.size > 1) NavigationArrows(
            { TileSwitcher.switch(false) },
            { TileSwitcher.switch(true) })
    }

    @Composable
    inline fun CommunicationBox(
        type: String = "MQTT",
        crossinline content: @Composable () -> Unit
    ) {
        var show by remember { mutableStateOf(settings.mqttTabShow) }
        var enabled by remember { mutableStateOf(dashboard.mqtt.isEnabled) }
        val rotation = if (show) 0f else 180f

        val angle: Float by animateFloatAsState(
            targetValue = if (rotation > 360 - rotation) {
                -(360 - rotation)
            } else rotation,
            animationSpec = tween(durationMillis = 200, easing = LinearEasing), label = ""
        )

        FrameBox(a = "Communication: ", b = type) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LabeledSwitch(
                        label = { Text("Enabled:", fontSize = 15.sp, color = Theme.colors.a) },
                        checked = enabled,
                        onCheckedChange = {
                            enabled = it
                            show = it
                            dashboard.mqtt.isEnabled = it
                            dashboard.daemon?.notifyConfigChanged()
                        }
                    )

                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            show = !show
                            settings.mqttTabShow = show
                        }
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_arrow), "",
                            tint = Theme.colors.a,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(angle)
                        )
                    }
                }

                AnimatedVisibility(visible = show) {
                    Column {
                        content()
                    }
                }
            }
        }
    }

    @Composable
    fun Notification(fragment: Fragment) {

        FrameBox(a = "Logging") {
            Column {
                var log by remember { mutableStateOf(tile.mqtt.doLog) }
                LabeledSwitch(
                    label = { Text("Log new values:", fontSize = 15.sp, color = Theme.colors.a) },
                    checked = log,
                    onCheckedChange = {
                        log = it
                        tile.mqtt.doLog = it
                    },
                )
            }
        }

        FrameBox(a = "Notifications") {
            Column {
                var notify by remember { mutableStateOf(tile.mqtt.doNotify) }
                LabeledSwitch(
                    label = {
                        Text(
                            "Notify on receive:",
                            fontSize = 15.sp,
                            color = Theme.colors.a
                        )
                    },
                    checked = notify,
                    onCheckedChange = {
                        if (fragment.activity?.areNotificationsAllowed() == true || !it) {
                            notify = it
                            tile.mqtt.doNotify = it
                        } else fragment.activity?.requestNotifications()
                    },
                )

                var quiet by remember { mutableStateOf(tile.mqtt.silentNotify) }
                AnimatedVisibility(visible = notify) {
                    Column {
                        LabeledCheckbox(
                            label = {
                                Text(
                                    "Make notification quiet",
                                    fontSize = 15.sp,
                                    color = Theme.colors.a
                                )
                            },
                            checked = quiet,
                            onCheckedChange = {
                                quiet = it
                                tile.mqtt.silentNotify = it
                            },
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        var notifyPayload by remember { mutableStateOf(tile.mqtt.notifyPayload) }
                        EditText(
                            label = { Text("Notification payload") },
                            value = notifyPayload,
                            onValueChange = {
                                notifyPayload = it
                                tile.mqtt.notifyPayload = it
                            },
                            modifier = Modifier.padding(top = 0.dp)
                        )

                        var notifyTitle by remember { mutableStateOf(tile.mqtt.notifyTitle) }
                        EditText(
                            label = { Text("Notification title") },
                            value = notifyTitle,
                            onValueChange = {
                                notifyTitle = it
                                tile.mqtt.notifyTitle = it
                            },
                            modifier = Modifier.padding(top = 10.dp)
                        )

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                        )

                        Text(
                            "Use @v to insert current tile value",
                            fontSize = 13.sp,
                            color = Theme.colors.a
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun PairList(
        options: MutableList<Pair<String, String>>,
        onRemove: (Int) -> Unit = {},
        onAdd: () -> Unit = {},
        onFirst: (Int, String) -> Unit = { _, _ -> },
        onSecond: (Int, String) -> Unit = { _, _ -> }
    ) {
        val options = remember { options.toMutableStateList() }

        FrameBox(a = "Modes list") {
            Column {
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier
                        .padding(end = 32.dp, top = 0.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "ALIAS",
                        fontSize = 13.sp,
                        color = Theme.colors.a,
                        letterSpacing = 2.sp
                    )
                    Text(
                        "PAYLOAD",
                        fontSize = 13.sp,
                        color = Theme.colors.a,
                        letterSpacing = 2.sp
                    )
                }

                options.forEachIndexed { index, pair ->
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        EditText(
                            label = {},
                            value = pair.first,
                            onValueChange = {
                                options[index] = options[index].copy(first = it)
                                onFirst(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                        )

                        EditText(
                            label = {},
                            value = pair.second,
                            onValueChange = {
                                options[index] = options[index].copy(second = it)
                                onSecond(index, it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp)
                        )

                        Icon(
                            painterResource(R.drawable.il_interface_multiply),
                            "",
                            tint = Theme.colors.b,
                            modifier = Modifier
                                .padding(start = 10.dp, bottom = 13.dp)
                                .size(30.dp)
                                .nrClickable {
                                    if (options.size > 2) {
                                        options.removeAt(index)
                                        onRemove(index)
                                    }
                                }
                        )
                    }
                }

                BasicButton(
                    contentPadding = PaddingValues(13.dp),
                    onClick = {
                        options.add("" to "")
                        onAdd()
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                ) {
                    Text("ADD OPTION", color = Theme.colors.a)
                }
            }
        }
    }
}