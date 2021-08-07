package com.netDashboard.folder_tree

object FolderTree {

    var rootFolder: String = ""
    val dashboardsFile
        get() = "$rootFolder/dashboards"
    val tilesFile
        get() = "$rootFolder/tiles"

    //fun buildPath(path: String) {
    //    val f = File(path)
    //    if (!f.isDirectory) f.mkdirs()
    //}
}