package com.alteratom.dashboard.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alteratom.BuildConfig
import com.alteratom.R
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activity.MainActivity.Companion.fm
import com.alteratom.dashboard.activity.PayActivity
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.areNotificationsAllowed
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.helper_objects.Debug
import com.alteratom.dashboard.helper_objects.Setup
import com.alteratom.dashboard.helper_objects.Storage.mapper
import com.alteratom.dashboard.helper_objects.Storage.parseListSave
import com.alteratom.dashboard.helper_objects.Storage.parseSave
import com.alteratom.dashboard.helper_objects.Storage.prepareSave
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.openBatterySettings
import com.alteratom.dashboard.requestNotifications
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

class SettingsFragment : Fragment() {

    private lateinit var open: ActivityResultLauncher<Intent>
    private lateinit var create: ActivityResultLauncher<Intent>

    var pro = MutableLiveData(aps.isLicensed)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        create =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        val backup = arrayOf(
                            aps.dashboards.prepareSave(),
                            aps.settings.prepareSave(),
                            aps.theme.prepareSave()
                        )

                        try {
                            requireContext().contentResolver.openFileDescriptor(uri, "w")
                                ?.use { it ->
                                    FileOutputStream(it.fileDescriptor).use {
                                        it.write(mapper.writeValueAsString(backup).toByteArray())
                                    }
                                }

                            Handler(Looper.getMainLooper()).postDelayed({
                                createToast(requireContext(), "Backup successful")
                            }, 100)
                        } catch (e: Exception) {
                            Debug.recordException(e)
                            createToast(requireContext(), "Backup failed")
                        }
                    }
                }
            }

        open =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        try {
                            val stringBuilder = StringBuilder()
                            requireContext().contentResolver.openInputStream(uri)
                                ?.use { inputStream ->
                                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                                        var line: String? = reader.readLine()
                                        while (line != null) {
                                            stringBuilder.append(line)
                                            line = reader.readLine()
                                        }
                                    }
                                }

                            val backupString = stringBuilder.toString()

                            val backup: List<String> = try {
                                mapper.readerForListOf(String::class.java).readValue(backupString)
                            } catch (e: Exception) {
                                Debug.recordException(e)
                                listOf()
                            }

                            //Abort if empty
                            if (backup.isEmpty()) {
                                createToast(requireContext(), "Backup restore failed")
                                return@also
                            }

                            //Unpack
                            val d = parseListSave<Dashboard>(backup[0])
                            val s = parseSave<Settings>(backup[1])
                            val t = parseSave<Theme>(backup[2])

                            //Abort on fail
                            if (s == null || t == null) throw Exception("Backup restore failed")

                            //Search for any tile with doNotify enabled
                            if (d.any { it.tiles.any { tile -> tile.mqtt.doNotify } }) {
                                //Request notifications permissions
                                activity?.apply {
                                    if (!areNotificationsAllowed()) requestNotifications()
                                }
                            }

                            Setup.applyConfig(d, s, t)
                        } catch (e: Exception) {
                            Debug.recordException(e)
                            createToast(requireContext(), "Backup restore failed")
                        }
                    }
                }
            }
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        open.launch(intent)
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "atomDashboard.backup")
        }
        create.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeConstruct(requireContext()) {

        Box(contentAlignment = Alignment.BottomCenter) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Settings", fontSize = 45.sp, color = colors.color)

                FrameBox("Optionals") {
                    Column {
                        var anim by remember { mutableStateOf(aps.settings.animateUpdate) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Animate tile update:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = anim,
                            onCheckedChange = {
                                anim = it
                                aps.settings.animateUpdate = it
                            },
                        )

                        var last by remember { mutableStateOf(aps.settings.startFromLast) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Last dashboard on start:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = last,
                            onCheckedChange = {
                                last = it
                                aps.settings.startFromLast = it
                            },
                        )

                        var switch by remember { mutableStateOf(aps.settings.hideNav) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Hide navigation arrows:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = switch,
                            onCheckedChange = {
                                switch = it
                                aps.settings.hideNav = it
                            },
                        )

                        var military by remember { mutableStateOf(aps.settings.militaryTime) }
                        LabeledSwitch(
                            label = {
                                Column {
                                    Text(
                                        "24-hour log time format:",
                                        fontSize = 15.sp,
                                        color = colors.a
                                    )
                                    Text(
                                        "Applies for new entries",
                                        fontSize = 10.sp,
                                        color = colors.b
                                    )
                                }
                            },
                            checked = military,
                            onCheckedChange = {
                                military = it
                                aps.settings.militaryTime = military
                            },
                        )

                        var notifyStack by remember { mutableStateOf(aps.settings.notifyStack) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "New notifications\noverwrite previous:",
                                    fontSize = 14.sp,
                                    color = colors.a
                                )
                            },
                            checked = notifyStack,
                            onCheckedChange = {
                                notifyStack = it
                                aps.settings.notifyStack = notifyStack
                            },
                        )
                    }
                }

                var workShow by remember { mutableStateOf(false) }

                if (workShow) {
                    Dialog({ workShow = false }) {
                        Column(
                            modifier = Modifier
                                .padding(bottom = 20.dp)
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    BorderStroke(1.dp, colors.color), RoundedCornerShape(6.dp)
                                )
                                .background(colors.background.copy(.8f))
                                .padding(15.dp),
                        ) {
                            Text(
                                "Battery optimization must be disabled to use this option",
                                fontSize = 18.sp,
                                color = colors.a
                            )

                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                onClick = {
                                    context?.openBatterySettings()
                                    workShow = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 14.dp)
                            ) {
                                Text(
                                    "BATTERY OPTIMIZATION SETTINGS",
                                    fontSize = 10.sp,
                                    color = colors.a
                                )
                            }
                        }
                    }
                }

                FrameBox("Background work") {
                    Column {
                        var fgEnabled by remember { mutableStateOf(aps.settings.fgEnabled) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Enable background work:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = fgEnabled,
                            onCheckedChange = {
                                if (context?.isBatteryOptimized() == false || !it) {
                                    fgEnabled = it
                                    aps.settings.fgEnabled = fgEnabled
                                    Setup.applyConfig(aps.dashboards, aps.settings, aps.theme)
                                } else workShow = true
                            },
                        )

                        Text(
                            "This option enables the app to operate in " +
                                    "the background by creating a persistent notification. " +
                                    "However this can affect battery life and it " +
                                    "does not guarantee that the app will not be terminated by the system.",
                            fontSize = 13.sp,
                            color = colors.b
                        )
                    }
                }

                FrameBox("Theme") {
                    BasicButton(
                        contentPadding = PaddingValues(13.dp),
                        onClick = { fm.replaceWith(ThemeFragment()) }
                    ) {
                        Text("EDIT THEME", fontSize = 10.sp, color = colors.a)
                    }
                }

                FrameBox("Backup") {
                    Row {
                        BasicButton(
                            contentPadding = PaddingValues(13.dp),
                            modifier = Modifier.weight(.47f),
                            onClick = { createFile() }
                        ) {
                            Text("CREATE", fontSize = 10.sp, color = colors.a)
                        }

                        Spacer(modifier = Modifier.weight(.06f))

                        BasicButton(
                            contentPadding = PaddingValues(13.dp),
                            modifier = Modifier.weight(.47f),
                            onClick = { openFile() }
                        ) {
                            Text("RESTORE", fontSize = 10.sp, color = colors.a)
                        }
                    }
                }

                FrameBox("About") {
                    Column {
                        BasicButton(
                            contentPadding = PaddingValues(13.dp),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                activity?.apply {
                                    Intent(
                                        this,
                                        PayActivity::class.java
                                    ).also {
                                        it.action = Intent.ACTION_VIEW
                                        startActivity(it)
                                    }
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "SUPPORT DEVELOPMENT ",
                                    fontSize = 10.sp,
                                    color = colors.a
                                )
                                Icon(
                                    painterResource(R.drawable.il_shape_heart_alt),
                                    "",
                                    tint = colors.a,
                                    modifier = Modifier.height(10.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                )
            }

            val pro by pro.observeAsState()
            Text(
                "${if (pro ?: aps.isLicensed) "pro" else "free"} | ${BuildConfig.VERSION_NAME}",
                Modifier
                    .padding(bottom = 5.dp)
                    .align(Alignment.BottomCenter),
                fontSize = 10.sp,
                color = colors.c
            )
        }

    }

    override fun onResume() {
        super.onResume()
        pro.postValue(aps.isLicensed)
    }
}