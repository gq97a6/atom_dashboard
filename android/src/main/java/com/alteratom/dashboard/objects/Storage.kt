package com.alteratom.dashboard.objects

import com.alteratom.dashboard.objects.G.path
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
            val path = path[this::class]
            File(path!!).writeText(save)
        } catch (e: Exception) {
            Debug.recordException(e)
        }
    }

    //Save serialized collection of objects to file
    inline fun <reified T> Collection<T>.saveToFile(save: String = this.prepareSave()) {
        try {
            val path = path[T::class]
            File(path!!).writeText(save)
        } catch (e: Exception) {
            Debug.recordException(e)
        }
    }

    //Get string from file
    inline fun <reified T> getSave() = try {
        FileReader(path[T::class]).readText()
    } catch (e: Exception) {
        Debug.recordException(e)
        ""
    }

    //Deserialize object from string
    inline fun <reified T> parseSave(save: String = getSave<T>()): T? =
        try {
            mapper.readValue(save, T::class.java)
        } catch (e: Exception) {
            Debug.recordException(e)
            null
        }

    //Deserialize collection of objects from string
    inline fun <reified T> parseListSave(save: String = getSave<T>()): MutableList<T> =
        try {
            mapper.readerForListOf(T::class.java).readValue(save)
        } catch (e: Exception) {
            Debug.recordException(e)
            mutableListOf()
        }
}