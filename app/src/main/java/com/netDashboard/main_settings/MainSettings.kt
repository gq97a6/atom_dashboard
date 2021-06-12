package com.netDashboard.main_settings

import com.netDashboard.folder_tree.FolderTree
import java.io.*

class MainSettings(private val rootPath: String) : Serializable {

    var lastDashboardName: String? = null

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

    fun getSaved(): MainSettings {

        if (!FolderTree(rootFolder).check()) return MainSettings(rootPath)

        return try {
            val file = FileInputStream(fileName)
            val inStream = ObjectInputStream(file)

            val settings = inStream.readObject() as MainSettings

            inStream.close()
            file.close()

            settings
        } catch (e: Exception) {
            MainSettings(rootPath)
        }
    }
}