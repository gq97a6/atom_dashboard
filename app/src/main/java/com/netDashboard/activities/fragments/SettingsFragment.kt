package com.netDashboard.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.*
import com.netDashboard.Settings.Companion.saveToFile
import com.netDashboard.Theme.Companion.saveToFile
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.SplashScreenActivity
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.dashboard.Dashboards.Companion.saveToFile
import com.netDashboard.databinding.FragmentSettingsBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader


class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private lateinit var b: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewConfig()
        theme.apply(b.root, requireContext())

        b.sLast.setOnClickListener {
            settings.startFromLast = b.sLast.isChecked
        }

        b.sThemeEdit.setOnClickListener {
            (activity as MainActivity).fm.replaceWith(ThemeFragment())
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
        b.sLast.isChecked = settings.startFromLast
        b.sThemeIsDark.isChecked = theme.a.isDark
    }

    val OPEN_FILE = 2
    fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, OPEN_FILE)
    }

    val CREATE_FILE = 1
    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "backup.txt")

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            //putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?
    ) {
        when (requestCode) {
            CREATE_FILE -> {
                resultData?.data?.also { uri ->
                    val backup = arrayOf(dashboards.prepareSave(), settings.prepareSave(), theme.prepareSave())

                    try {
                        requireContext().contentResolver.openFileDescriptor(uri, "w")?.use {
                            FileOutputStream(it.fileDescriptor).use {
                                it.write(G.mapper.writeValueAsString(backup).toByteArray())
                            }
                        }

                        createToast(requireContext(), "Backup successful")
                    } catch (e: java.lang.Exception) {
                        createToast(requireContext(), "Backup failed")
                    }
                }
            }
            OPEN_FILE -> {
                resultData?.data?.also { uri ->
                    try {
                        val stringBuilder = StringBuilder()
                        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
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
                            val d = Dashboards.parseSave(backup[0])
                            val s = Settings.parseSave(backup[1])
                            val t = Theme.parseSave(backup[2])

                            if (d != null) G.dashboards = d
                            if (s != null) settings = s
                            if (t != null) theme = t

                            G.dashboards.saveToFile()
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
        //resultData?.data?.also { uri ->
        //    run {}
        //    // Perform operations on the document using its URI.
        //}
    }
}