package com.alteratom.dashboard.helper_objects

import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.io.FileReader

object Storage {
    val mapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    //Serialize object to string
    fun Any.prepareSave(): String = mapper.writeValueAsString(this)

    //Save serialized object to file
    fun Any.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = aps.path[this::class]
            File(path!!).writeText(save)
        } catch (_: Exception) {
            null
        }
    }

    //Save serialized collection of objects to file
    inline fun <reified T> Collection<T>.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = aps.path[T::class]
            File(path!!).writeText(save)
        } catch (_: Exception) {
            null
        }
    }

    //Get string from file
    inline fun <reified T> getSave() = try {
        FileReader(aps.path[T::class]).readText()
    } catch (_: Exception) {
        ""
    }

    //Deserialize object from string
    inline fun <reified T> parseSave(save: String = getSave<T>()): T? =
        try {
            mapper.readValue(save, T::class.java)
        } catch (_: Exception) {
            null
        }

    //Deserialize collection of objects from string
    inline fun <reified T> parseListSave(save: String = getSave<T>()): MutableList<T> =
        try {
            mapper.readerForListOf(T::class.java).readValue(save)
        } catch (_: Exception) {
            mutableListOf()
        }
}