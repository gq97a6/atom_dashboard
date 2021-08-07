package com.netDashboard.folder_tree

import java.io.File

object FolderTree {

    //Folders
    var rootFolder: String = ""
    val dashboardRootFolder
        get() = "$rootFolder/dashboards"
    fun dashboardFolder(name: String): String = "$dashboardRootFolder/$name"

    //Files
    fun tilesFile(name: String): String = "$dashboardRootFolder/$name/tiles"
    fun dashboardFile(name: String): String = "$dashboardRootFolder/$name/dashboard"
    val settingsFile
        get() = "$rootFolder/settings"

    fun build() {
        buildPath("$rootFolder/dashboards")
    }

    fun buildPath(path: String) {
        val f = File(path)
        if (!f.isDirectory) f.mkdirs()
    }
}