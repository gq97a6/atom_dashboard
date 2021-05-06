package com.netDashboard.dashboard_settings_activity

import java.io.*

class DashboardSettings : Serializable {
    var name = ""
    var spanCount = 3

    fun saveSettings(settings: DashboardSettings, name: String) {

        val file = FileOutputStream(name)

        val outStream = ObjectOutputStream(file)

        outStream.writeObject(settings)

        outStream.close()
        file.close()
    }

    fun getSettings(name: String): DashboardSettings {
        return try {
            val file = FileInputStream(name)
            val inStream = ObjectInputStream(file)

            val settings = inStream.readObject() as DashboardSettings

            inStream.close()
            file.close()

            settings
        } catch (e: Exception) {
            DashboardSettings()
        }
    }
}