package com.alteratom.dashboard

object FolderTree {
    var rootFolder: String = ""

    val dashboardsFile
        get() = "$rootFolder/dashboards"
    val settingsFile
        get() = "$rootFolder/settings"
    val themeFile
        get() = "$rootFolder/theme"
}