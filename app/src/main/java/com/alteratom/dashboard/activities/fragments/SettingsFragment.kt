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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.*
import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.FolderTree.parseSave
import com.alteratom.dashboard.FolderTree.prepareSave
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.SetupActivity
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.dashboard.Dashboard
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader


class SettingsFragment : Fragment() {

    private lateinit var open: ActivityResultLauncher<Intent>
    private lateinit var create: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())

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
                                        it.write(G.mapper.writeValueAsString(backup).toByteArray())
                                    }
                                }

                            Handler(Looper.getMainLooper()).postDelayed({
                                createToast(requireContext(), "Backup successful")
                            }, 100)
                        } catch (e: java.lang.Exception) {
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
                                G.mapper.readerForListOf(String::class.java).readValue(backupString)
                            } catch (e: Exception) {
                                listOf()
                            }

                            if (backup.isNotEmpty()) {
                                val d = parseListSave<Dashboard>(backup[0])
                                val s = parseSave<Settings>(backup[1])
                                val t = parseSave<Theme>(backup[2])

                                if (d != null) dashboards = d
                                if (s != null) settings = s
                                if (t != null) theme = t

                                dashboards.saveToFile()
                                settings.saveToFile()
                                theme.saveToFile()

                                activity?.startActivity(
                                    Intent(
                                        context,
                                        SetupActivity::class.java
                                    )
                                )
                                activity?.finish()
                                activity?.finishAffinity()
                            } else {
                                createToast(requireContext(), "Backup restore failed")
                            }
                        } catch (e: java.lang.Exception) {
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
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(Theme.isDark) {
                    //Background
                    Box(modifier = Modifier.background(colors.background))

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                                    }
                                }

                                FrameBox("Theme") {
                                    OutlinedButton(
                                        contentPadding = PaddingValues(13.dp),
                                        border = BorderStroke(2.dp, colors.b),
                                        onClick = { fm.replaceWith(ThemeFragment()) }
                                    ) {
                                        Text("EDIT THEME", fontSize = 10.sp, color = colors.a)
                                    }
                                }

                                FrameBox("Backup") {
                                    Row {
                                        OutlinedButton(
                                            contentPadding = PaddingValues(13.dp),
                                            border = BorderStroke(2.dp, colors.b),
                                            modifier = Modifier.weight(.47f),
                                            onClick = { createFile() }
                                        ) {
                                            Text("CREATE", fontSize = 10.sp, color = colors.a)
                                        }

                                        Spacer(modifier = Modifier.weight(.06f))

                                        OutlinedButton(
                                            contentPadding = PaddingValues(13.dp),
                                            border = BorderStroke(2.dp, colors.b),
                                            modifier = Modifier.weight(.47f),
                                            onClick = { openFile() }
                                        ) {
                                            Text("RESTORE", fontSize = 10.sp, color = colors.a)
                                        }
                                    }
                                }

                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}