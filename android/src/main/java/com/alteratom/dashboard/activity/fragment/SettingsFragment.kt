package com.alteratom.dashboard.activity.fragment

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.activity.PayActivity
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.`object`.FragmentManager.fm
import com.alteratom.dashboard.`object`.G.dashboards
import com.alteratom.dashboard.`object`.G.fc
import com.alteratom.dashboard.`object`.G.settings
import com.alteratom.dashboard.`object`.G.theme
import com.alteratom.dashboard.`object`.Pro
import com.alteratom.dashboard.`object`.Setup
import com.alteratom.dashboard.`object`.Storage.mapper
import com.alteratom.dashboard.`object`.Storage.parseListSave
import com.alteratom.dashboard.`object`.Storage.parseSave
import com.alteratom.dashboard.`object`.Storage.prepareSave
import com.alteratom.dashboard.`object`.Storage.saveToFile
import com.alteratom.dashboard.openBatterySettings
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader

class SettingsFragment : Fragment() {

    private lateinit var open: ActivityResultLauncher<Intent>
    private lateinit var create: ActivityResultLauncher<Intent>

    var pro = MutableLiveData(Pro.status)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        create =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uri ->
                        val backup = arrayOf(
                            dashboards.prepareSave(),
                            settings.prepareSave(),
                            theme.prepareSave()
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
                                listOf()
                            }

                            if (backup.isNotEmpty()) {
                                val d = parseListSave<Dashboard>(backup[0])
                                val s = parseSave<Settings>(backup[1])
                                val t = parseSave<Theme>(backup[2])

                                DaemonsManager.notifyAllDischarged()
                                dashboards = d
                                DaemonsManager.notifyAllAssigned(requireContext())

                                if (s != null) settings = s
                                if (t != null) theme = t

                                dashboards.saveToFile()
                                settings.saveToFile()
                                theme.saveToFile()

                                //Rerun setup
                                CoroutineScope(Dispatchers.Default).launch {
                                    Setup.apply {
                                        val a = requireActivity() as MainActivity
                                        showFragment()
                                        proStatus()
                                        billing(a)
                                        batteryCheck(a)
                                        setCase()
                                        service(a)
                                        globals()
                                        permissions(a)
                                        daemons(a)
                                        hideFragment()
                                    }
                                }
                            } else {
                                createToast(requireContext(), "Backup restore failed")
                            }
                        } catch (e: Exception) {
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
                        var anim by remember { mutableStateOf(settings.animateUpdate) }
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
                                settings.animateUpdate = it
                            },
                        )

                        var last by remember { mutableStateOf(settings.startFromLast) }
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
                                settings.startFromLast = it
                            },
                        )

                        var switch by remember { mutableStateOf(settings.hideNav) }
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
                                settings.hideNav = it
                            },
                        )

                        var military by remember { mutableStateOf(settings.militaryTime) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "24-hour log time format:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = switch,
                            onCheckedChange = {
                                military = it
                                settings.militaryTime = military
                            },
                        )

                        var notifyStack by remember { mutableStateOf(settings.notifyStack) }
                        LabeledSwitch(
                            label = {
                                Text(
                                    "New notifications\noverwrite previous:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = notifyStack,
                            onCheckedChange = {
                                notifyStack = it
                                settings.notifyStack = notifyStack
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
                                    BorderStroke(1.dp, colors.color),
                                    RoundedCornerShape(6.dp)
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
                                border = BorderStroke(2.dp, colors.b),
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

                @Composable
                fun tmpLabel() {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Background work",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.a,
                            modifier = Modifier.padding(
                                start = 5.dp,
                                bottom = 3.dp,
                                top = 15.dp
                            )
                        )

                        Text(
                            text = "NEW",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.b,
                            modifier = Modifier
                                .padding(
                                    end = 5.dp,
                                    bottom = 3.dp,
                                    top = 15.dp
                                )
                        )
                    }
                }

                FrameBox({ tmpLabel() }) {
                    Column {
                        var fgEnabled by remember { mutableStateOf(settings.fgEnabled) }
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
                                    settings.fgEnabled = fgEnabled

                                    //Rerun setup
                                    CoroutineScope(Dispatchers.Default).launch {
                                        Setup.apply {
                                            val a = requireActivity() as MainActivity
                                            showFragment()
                                            batteryCheck(a)
                                            setCase()
                                            service(a)
                                            daemons(a)
                                            hideFragment()
                                        }
                                    }

                                } else workShow = true
                            },
                        )

                        Text(
                            "This option allows the app to work in the background. " +
                                    "To do so persistent notification is created. " +
                                    "It is not guaranteed that the app won't be killed by the system.",
                            fontSize = 13.sp,
                            color = colors.b
                        )
                    }
                }

                FrameBox("Theme") {
                    BasicButton(
                        contentPadding = PaddingValues(13.dp),
                        border = BorderStroke(2.dp, colors.b),
                        onClick = { fm.replaceWith(ThemeFragment()) }
                    ) {
                        Text("EDIT THEME", fontSize = 10.sp, color = colors.a)
                    }
                }

                FrameBox("Backup") {
                    Row {
                        BasicButton(
                            contentPadding = PaddingValues(13.dp),
                            border = BorderStroke(2.dp, colors.b),
                            modifier = Modifier.weight(.47f),
                            onClick = { createFile() }
                        ) {
                            Text("CREATE", fontSize = 10.sp, color = colors.a)
                        }

                        Spacer(modifier = Modifier.weight(.06f))

                        BasicButton(
                            contentPadding = PaddingValues(13.dp),
                            border = BorderStroke(2.dp, colors.b),
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
                            border = BorderStroke(2.dp, colors.b),
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
                "${if (pro ?: Pro.status) "pro" else "free"} | ${BuildConfig.VERSION_NAME}",
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
        pro.postValue(Pro.status)
    }
}