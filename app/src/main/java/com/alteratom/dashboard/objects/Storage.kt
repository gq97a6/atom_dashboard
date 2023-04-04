package com.alteratom.dashboard.objects

import android.util.Log
import com.alteratom.dashboard.objects.G.path
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.FileReader

object Storage {
    val mapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun Any.prepareSave(): String = mapper.writeValueAsString(this)

    fun Any.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[this::class]
            File(path).writeText(save)
        } catch (e: Exception) {
             Log.e("ALTER", e.stackTraceToString())
        }
    }

    inline fun <reified T> Collection<T>.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[T::class]
            File(path).writeText(save)
        } catch (e: Exception) {
             Log.e("ALTER", e.stackTraceToString())
        }
    }

    inline fun <reified T> getSave() = try {
        FileReader(path[T::class]).readText()
    } catch (e: Exception) {
        ""
    }

    inline fun <reified T> parseSave(save: String = getSave<T>()): T? =
        try {
            mapper.readValue(save, T::class.java)
        } catch (e: Exception) {
            null
        }

    inline fun <reified T> parseListSave(save: String = getSave<T>()): MutableList<T> =
        try {
            mapper.readerForListOf(T::class.java).readValue(save)
        } catch (e: Exception) {
            try {
                mapper.readerForListOf(T::class.java).readValue(save.fixSave())
            } catch (e: Exception) {
                mutableListOf()
            }
        }

    fun String.fixSave(): String {
        var s = this.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.thermostat.", "")
        s = s.replace("com.alteratom.tile.types.time.", "")
        s = s.replace("com.alteratom.tile.types.button.", "")
        s = s.replace("com.alteratom.tile.types.pick.", "")
        s = s.replace("com.alteratom.tile.types.lights.", "")
        s = s.replace("com.alteratom.tile.types.color.", "")
        s = s.replace("com.alteratom.tile.types.switch.", "")
        s = s.replace("com.alteratom.tile.types.slider.", "")
        s = s.replace("com.alteratom.tile.types.terminal.", "")
        return s
    }
}