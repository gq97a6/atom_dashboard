package com.netDashboard.folder_tree

import java.io.File

object FolderTree {

    var rootFolder: String = ""
    val dashboardsFolder
        get() = "$rootFolder/dashboards"

    fun tilesFile(name: String): String = "$dashboardsFolder/$name/tiles"
    fun dashboardFile(name: String): String = "$dashboardsFolder/$name/dashboard"
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