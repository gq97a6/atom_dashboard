package com.netDashboard.main

import java.io.*

class Settings(private val rootPath: String) : Serializable {

    var lastDashboardName:String? = null

    private val rootFolder = "$rootPath/app_data/"
    private val fileName = "$rootFolder/settings/"

    fun save() {

        FolderTree(rootFolder).check()

        try {
            val file = FileOutputStream(fileName)

            val outStream = ObjectOutputStream(file)

            outStream.writeObject(this)

            outStream.close()
            file.close()
        } catch (e: Exception) {
        }
    }

    fun getSaved(): Settings {

        if (!FolderTree(rootFolder).check()) return Settings(rootPath)

        return try {
            val file = FileInputStream(fileName)
            val inStream = ObjectInputStream(file)

            val settings = inStream.readObject() as Settings

            inStream.close()
            file.close()

            settings
        } catch (e: Exception) {
            Settings(rootPath)
        }
    }
}