package com.netDashboard.main

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class DashboardsNames {

    fun get(rootPath: String): List<String> {
        return try {
            val file = FileInputStream("$rootPath/dashboardsNames")
            val inStream = ObjectInputStream(file)

            val list = inStream.readObject() as List<*>

            inStream.close()
            file.close()

            list.filterIsInstance<String>().takeIf { it.size == list.size } ?: listOf()
        } catch (e: Exception) {
            listOf()
        }
    }

    fun save(rootPath: String) {
        try {
            val file = FileOutputStream("$rootPath/dashboardsNames")

            val outStream = ObjectOutputStream(file)

            outStream.writeObject(this)

            outStream.close()
            file.close()
        } catch (e: java.lang.Exception) {
        }
    }
}