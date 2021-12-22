package com.netDashboard

import com.netDashboard.globals.G.mapper
import java.io.File
import java.io.FileReader

class Settings {

    var lastDashboardId: Long? = null
    var startFromLast: Boolean = false

    companion object {
        fun getSaved(): Settings =
            try {
                mapper.readValue(FileReader(FolderTree.settingsFile), Settings::class.java)
            } catch (e: Exception) {
                Settings()
            }
    }

    fun save() {
        try {
            File(FolderTree.settingsFile).writeText(mapper.writeValueAsString(this))
        } catch (e: Exception) {
            run { }
        }
    }
}