package com.alteratom.dashboard

import com.alteratom.dashboard.dashboard.Dashboard
import java.io.File
import java.io.FileReader
import kotlin.reflect.KClass

object FolderTree {
    var rootFolder: String = ""
        set(value) {
            field = value
            path = mapOf(
                Theme::class to "$value/theme",
                Settings::class to "$value/settings",
                Dashboard::class to "$value/dashboards",
            )
        }

    lateinit var path: Map<KClass<out Any>, String>

    fun Any.prepareSave(): String = G.mapper.writeValueAsString(this)

    fun Any.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[(if (this is Collection<*>) this.first()!! else this)::class]
            File(path).writeText(save)
        } catch (e: Exception) {
            run { }
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