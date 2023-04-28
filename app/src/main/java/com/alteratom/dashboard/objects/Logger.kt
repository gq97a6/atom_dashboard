package com.alteratom.dashboard.objects

import android.os.Environment
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Logger {
    private val folder =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    val file = File("${folder.absolutePath}/log.txt")

    val now
        get() = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd:HH:mm:ss"))

    init {
        try {
            if (!file.exists()) file.createNewFile()
        } catch (_: Exception) {
        }
    }

    fun log(s: String) {
        try {
            //file.appendText("[$now]: $s\n")
        } catch (_: Exception) {
        }
    }
}