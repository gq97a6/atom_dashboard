package com.netDashboard.settings

import com.google.gson.Gson
import com.netDashboard.folder_tree.FolderTree.settingsFile
import java.io.File
import java.io.FileReader

object Settings {
    var lastDashboardId: Long? = null
    var startFromLast: Boolean = false

    var colorPrimary = 0
    var colorSecondary = 0
    var colorBackground = 0

    init {
        getSaved()
    }

    fun save() {
        try {
            File(settingsFile).writeText(Gson().toJson(this))
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getSaved() {

    }
}