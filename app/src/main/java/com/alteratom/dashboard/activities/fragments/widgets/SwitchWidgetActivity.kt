package com.alteratom.dashboard.activities.fragments.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alteratom.dashboard.*
import com.alteratom.dashboard.FolderTree.parseSave
import com.alteratom.dashboard.FolderTree.rootFolder
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.widgetDataHolder
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.fragments.*
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesMqttCompose
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.widgets.SwitchWidgetProvider
import com.alteratom.dashboard.widgets.WidgetDataHolder
import com.alteratom.tile.types.switch.SwitchTile

class SwitchWidgetActivity : AppCompatActivity() {
    private var id = INVALID_APPWIDGET_ID

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        setResult(RESULT_CANCELED)

        intent.extras?.let {
            id = it.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        }

        if (id == INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        rootFolder = filesDir.canonicalPath.toString()
        G.theme.apply(context = this)

        setContent {
            ComposeTheme(Theme.isDark) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    val context = this@SwitchWidgetActivity
                    BasicButton(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth(),
                        onClick = {
                            val holder = try {
                                widgetDataHolder
                            } catch (e: Exception) {
                                parseSave<WidgetDataHolder>() ?: WidgetDataHolder()
                            }

                            holder.switch[id] = SwitchWidgetProvider.Data()
                            holder.saveToFile()

                            SwitchWidgetProvider.updateWidget(
                                context,
                                AppWidgetManager.getInstance(context),
                                id
                            )

                            setResult(RESULT_OK, Intent().putExtra(EXTRA_APPWIDGET_ID, id))
                            finish()
                        }
                    ) {
                        Text("ADD WIDGET", fontSize = 10.sp, color = colors.a)
                    }

                    val tile = G.tile as SwitchTile
                    FrameBox(a = "Communication: MQTT") {
                        Column {
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

                                        MainActivity.fm.replaceWith(TileIconFragment())
                                    },
                                    border = BorderStroke(0.dp, tile.palletFalse.cc.color),
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

                                var off by remember {
                                    mutableStateOf(
                                        tile.mqtt.payloads["false"] ?: ""
                                    )
                                }
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

                                        MainActivity.fm.replaceWith(TileIconFragment())
                                    },
                                    border = BorderStroke(0.dp, tile.palletTrue.cc.color),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .width(52.dp)
                                ) {
                                    Icon(
                                        painterResource(tile.iconResTrue),
                                        "",
                                        tint = tile.palletTrue.cc.color
                                    )
                                }

                                var on by remember {
                                    mutableStateOf(
                                        tile.mqtt.payloads["true"] ?: ""
                                    )
                                }
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

                            TilePropertiesMqttCompose.Communication()
                            //DSTilePropertiesCompose.Notification()
                        }
                    }
                }
            }
        }
    }
}
