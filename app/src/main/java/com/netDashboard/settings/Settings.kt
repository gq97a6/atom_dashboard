package com.netDashboard.settings

import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.globals.G.gson
import java.io.File
import java.io.FileReader

class Settings {

    var lastDashboardId: Long? = null
    var startFromLast: Boolean = false

    companion object {
        fun getSaved(): Settings =
            try {
                gson.fromJson(FileReader(FolderTree.settingsFile), Settings::class.java)
            } catch (e: Exception) {
                Settings()
            }
    }

    fun save() {
        try {
            File(FolderTree.settingsFile).writeText(gson.toJson(this))
        } catch (e: Exception) {
            throw e
        }
    }
}