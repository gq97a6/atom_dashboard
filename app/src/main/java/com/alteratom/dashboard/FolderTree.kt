package com.alteratom.dashboard

import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.demons.Daemon
import java.io.File
import java.io.FileReader

object FolderTree {
    var rootFolder: String = ""

    val path = mapOf(
        Theme::class to "$rootFolder/theme",
        Settings::class to "$rootFolder/settings",
        Dashboard::class to "$rootFolder/dashboards",
        Daemon::class to "$rootFolder/daemons",
    )

    fun Any.prepareSave(): String = G.mapper.writeValueAsString(this)

    fun Any.saveToFile(save: String = this.prepareSave()) {
        try {
            File(path[this::class]).writeText(save)
        } catch (e: Exception) {
        }
    }

    inline fun <reified T> getSave() = try {
        FileReader(path[T::class]).readText()
    } catch (e: Exception) {
        ""
    }

    inline fun <reified T> parseSave(save: String = getSave<T>()): T? =
        try {
            G.mapper.readValue(save, T::class.java)
        } catch (e: Exception) {
            null
        }

    inline fun <reified T> parseListSave(save: String = getSave<T>()): MutableList<T> =
        try {
            G.mapper.readerForListOf(T::class.java).readValue(save)
        } catch (e: Exception) {
            mutableListOf()
        }
}