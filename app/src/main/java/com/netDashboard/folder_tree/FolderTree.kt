package com.netDashboard.folder_tree

import java.io.File

class FolderTree(private val rootFolder: String) {
    fun check(): Boolean {
        val f = File(rootFolder)
        return if (!f.isDirectory) {
            f.mkdirs()

            false
        } else {
            true
        }
    }
}