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
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.FolderTree.parseListSave
import com.alteratom.dashboard.FolderTree.parseSave
import com.alteratom.dashboard.FolderTree.prepareSave
import com.alteratom.dashboard.FolderTree.saveToFile
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.SplashScreenActivity
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Daemon
import com.alteratom.databinding.FragmentSettingsBinding
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader


class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var b: FragmentSettingsBinding

    private lateinit var open: ActivityResultLauncher<Intent>
    private lateinit var create: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

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
                                        SplashScreenActivity::class.java
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewConfig()
        theme.apply(b.root, requireContext())

        b.sAnimateUpdate.setOnClickListener {
            settings.animateUpdate = b.sAnimateUpdate.isChecked
        }

        b.sLast.setOnClickListener {
            settings.startFromLast = b.sLast.isChecked
        }

        b.sThemeEdit.setOnClickListener {
            fm.replaceWith(ThemeFragment())
        }

        b.sThemeIsDark.setOnClickListener {
            theme.a.isDark = b.sThemeIsDark.isChecked
            theme.apply((activity as MainActivity).b.root, requireContext())
        }

        b.dpBackupCreate.setOnClickListener {
            createFile()
        }

        b.dpBackupRestore.setOnClickListener {
            openFile()
        }
    }

    private fun viewConfig() {
        b.sAnimateUpdate.isChecked = settings.animateUpdate
        b.sLast.isChecked = settings.startFromLast
        b.sThemeIsDark.isChecked = theme.a.isDark
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
}