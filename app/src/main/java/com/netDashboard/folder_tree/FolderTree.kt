package com.netDashboard.folder_tree

object FolderTree {

    var rootFolder: String = ""
    val dashboardsFile
        get() = "$rootFolder/dashboards"
    val settingsFile
        get() = "$rootFolder/dashboards"
    val themeFile
        get() = "$rootFolder/dashboards"

    //fun buildPath(path: String) {
    //    val f = File(path)
    //    if (!f.isDirectory) f.mkdirs()
    //}
}