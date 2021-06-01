@file:Suppress("unused")

package com.netDashboard.dashboard

import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileList
import java.io.*

class Dashboard(rootPath: String, dashboardName: String) : Serializable {

    private val tilesFileName = "$rootPath/$dashboardName" + "_tiles"
    private val settingsFileName = "$rootPath/$dashboardName" + "_settings"

    var settings = Settings()
        get() = Settings().getSaved()
        set(value) {
            field = value
            field.save()
        }


    var tiles: List<Tile> = listOf()
        get() = field.getSaved()
        set(value) {
            field = value
            field.save()
        }

    fun List<Tile>.save() {

        for ((i, _) in this.withIndex()) {
            this[i].context = null
            this[i].holder = null
            this[i].mqttd = null

            this[i].editMode(false)
            this[i].flag(false)
        }

        try {
            val file = FileOutputStream(tilesFileName)

            val outStream = ObjectOutputStream(file)

            outStream.writeObject(this)

            outStream.close()
            file.close()
        } catch (e: Exception) {
        }
    }

    private fun List<Tile>.getSaved(): List<Tile> {
        return try {
            val file = FileInputStream(tilesFileName)
            val inStream = ObjectInputStream(file)

            val list = inStream.readObject() as List<*>

            inStream.close()
            file.close()

            list.filterIsInstance<Tile>().takeIf { it.size == list.size } ?: listOf()
        } catch (e: Exception) {
            TileList().getButtons()
        }
    }

    inner class Settings : Serializable {
        var spanCount = 3

        var mqttAddress = "tcp://"
        var mqttPort = 1883
        val mqttURI
            get() = "$mqttAddress:$mqttPort"

        fun save() {
            try {
                val file = FileOutputStream(settingsFileName)

                val outStream = ObjectOutputStream(file)

                outStream.writeObject(this)

                outStream.close()
                file.close()
            } catch (e: Exception) {
            }
        }

        fun getSaved(): Settings {
            return try {
                val file = FileInputStream(settingsFileName)
                val inStream = ObjectInputStream(file)

                val settings = inStream.readObject() as Settings

                inStream.close()
                file.close()

                settings
            } catch (e: Exception) {
                Settings()
            }
        }
    }
}