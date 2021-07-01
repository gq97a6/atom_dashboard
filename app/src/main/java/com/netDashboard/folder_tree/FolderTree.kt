package com.netDashboard.folder_tree

import java.io.File

object FolderTree {

    var rootFolder: String = ""
    val dashboardsFolder
        get() = "$rootFolder/dashboards"

    fun tilesPropertiesFile(name: String): String = "$dashboardsFolder/$name/tilesProperties"
    fun dashboardPropertiesFile(name: String): String = "$dashboardsFolder/$name/properties"
    val settingsFile
        get() = "$rootFolder/settings"

    fun build() {
        buildPath("$rootFolder/dashboards")
        buildPath(rootFolder)
    }

    fun buildDashboard(name: String) = buildPath("$dashboardsFolder/$name")

    private fun buildPath(path: String) {
        val f = File(path)
        if (!f.isDirectory) f.mkdirs()
    }
}