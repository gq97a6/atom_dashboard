package com.netDashboard

import com.netDashboard.globals.G.mapper
import java.io.File
import java.io.FileReader

class Settings {

    var lastDashboardId: Long = 0
    var startFromLast: Boolean = true
    var mqttTabShow: Boolean = true

    companion object {
        fun Settings.saveToFile(save: String = this.prepareSave()) {
            try {
                File(FolderTree.settingsFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        private fun getSaveFromFile() = try {
            FileReader(FolderTree.settingsFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): Settings? =
            try {
                mapper.readValue(save, Settings::class.java)
            } catch (e: Exception) {
                null
            }
    }
}