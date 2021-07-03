package com.netDashboard.folder_tree

import android.util.Log
import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type

object FolderTree {

    var rootFolder: String = ""
    val dashboardsFolder
        get() = "$rootFolder/dashboards"

    fun tilesPropertiesFile(name: String): String = "$dashboardsFolder/$name/tilesProperties"
    fun dashboardFile(name: String): String = "$dashboardsFolder/$name/properties"
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

    fun Any.save(fileName: String) {
        File(fileName)
            .writeText(Gson().toJson(this))
        Log.i("OUY", Gson().toJson(this))
    }

    fun getSaved(type: Type, fileName: String): Any {
        return Gson().fromJson(
            FileReader(fileName),
            type
        )
    }
}