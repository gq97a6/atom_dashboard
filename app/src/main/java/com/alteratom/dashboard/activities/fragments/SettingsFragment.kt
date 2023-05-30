package com.alteratom.dashboard.activities.fragments

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
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alteratom.BuildConfig
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.PayActivity
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.ComposeTheme
import com.alteratom.dashboard.compose_global.FrameBox
import com.alteratom.dashboard.compose_global.LabeledSwitch
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.objects.ActivityHandler.restart
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.objects.Pro
import com.alteratom.dashboard.objects.Storage.mapper
import com.alteratom.dashboard.objects.Storage.parseListSave
import com.alteratom.dashboard.objects.Storage.parseSave
import com.alteratom.dashboard.objects.Storage.prepareSave
import com.alteratom.dashboard.objects.Storage.saveToFile
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

                                DaemonsManager.notifyAllRemoved()
                                dashboards = d
                                DaemonsManager.notifyAllAdded(requireContext())

                                if (s != null) settings = s
                                if (t != null) theme = t

                                dashboards.saveToFile()
                                settings.saveToFile()
                                theme.saveToFile()

                                activity?.startActivity(
                                    Intent(
                                        requireContext(),
                                        MainActivity::class.java
                                    )
                                )
                                activity?.finish()
                                activity?.finishAffinity()
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
    ): View {
        theme.apply(context = requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(Theme.isDark) {
                    Box(
                        contentAlignment = Alignment.BottomCenter
                    ) {
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
                                }
                            }

                            var workShow by remember { mutableStateOf(false) }
                            var hasChanged by remember { mutableStateOf(false) }

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
                                                openBatterySettings(requireContext())
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

                            val tmpAlpha = rememberInfiniteTransition().animateFloat(
                                initialValue = 1f,
                                targetValue = 0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(500, 500),
                                    repeatMode = RepeatMode.Reverse,
                                )
                            )

                            @Composable
                            fun tmpLabel() {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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
                                        text = "IMPORTANT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = colors.a,
                                        modifier = Modifier
                                            .padding(
                                                end = 5.dp,
                                                bottom = 3.dp,
                                                top = 15.dp
                                            )
                                            .alpha(tmpAlpha.value)
                                    )
                                }
                            }

                            FrameBox({ tmpLabel() }) {
                                Column {
                                    var foregroundService by remember { mutableStateOf(settings.foregroundService) }
                                    LabeledSwitch(
                                        label = {
                                            Text(
                                                "Enable background work:",
                                                fontSize = 15.sp,
                                                color = colors.a
                                            )
                                        },
                                        checked = foregroundService,
                                        onCheckedChange = {
                                            if (!isBatteryOptimized(requireContext()) || !it) {
                                                hasChanged = true
                                                foregroundService = it
                                                settings.foregroundService = foregroundService
                                            } else workShow = true
                                        },
                                    )

                                    if (hasChanged) {
                                        BasicButton(
                                            contentPadding = PaddingValues(13.dp),
                                            border = BorderStroke(2.dp, colors.b),
                                            onClick = {
                                                requireActivity().restart()
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp, bottom = 20.dp)
                                        ) {
                                            Text(
                                                "RESTART TO APPLY CHANGES",
                                                fontSize = 10.sp,
                                                color = colors.a
                                            )
                                        }
                                    }

                                    Text(
                                        "This option allows app to work in the background after being closed." +
                                                "\nTo do so persistent notification is created\ndue to Android requirements." +
                                                "\nStill it is not guaranteed that the app will not be killed by system.",
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

                            //BasicButton(
                            //    contentPadding = PaddingValues(13.dp),
                            //    border = BorderStroke(2.dp, colors.b),
                            //    modifier = Modifier
                            //        .fillMaxWidth()
                            //        .padding(top = 12.dp),
                            //    onClick = {
                            //        lifecycleScope.launch {
                            //            BillingHandler(requireActivity()).apply {
                            //                enable()
//
                            //                getPurchases()?.find {
                            //                    it.products.contains(PRO)
                            //                }?.let {
                            //                    it.consume()
                            //                }
//
                            //                disable()
                            //                connectionHandler.awaitDone()
                            //            }
//
                            //            Pro.removeLocalLicence()
                            //            createToast(requireContext(), "DONE")
                            //        }
                            //    }
                            //) {
                            //    Text("PRO REMOVE", fontSize = 10.sp, color = colors.a)
                            //}

                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                            )
                        }

                        val pro by pro.observeAsState()

                        Text(
                            "${if (pro ?: Pro.status) "pro" else "free"} | ${BuildConfig.VERSION_NAME}",
                            Modifier.padding(bottom = 5.dp),
                            fontSize = 10.sp,
                            color = colors.c
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pro.postValue(Pro.status)
    }
}