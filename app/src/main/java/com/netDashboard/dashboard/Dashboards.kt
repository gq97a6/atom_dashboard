package com.netDashboard.dashboard

import com.netDashboard.FolderTree.dashboardsFile
import com.netDashboard.globals.G.mapper
import com.netDashboard.prepareSave
import java.io.File
import java.io.FileReader

class Dashboards {
    companion object {
        fun MutableList<Dashboard>.saveToFile(save: String = this.prepareSave()) {
            try {
                File(dashboardsFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        private fun getSaveFromFile() = try {
            FileReader(dashboardsFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): MutableList<Dashboard>? =
            try {
                mapper.readerForListOf(Dashboard::class.java).readValue(save)
            } catch (e: Exception) {
                null
            }
    }
}
