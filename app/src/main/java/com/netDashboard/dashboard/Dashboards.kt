package com.netDashboard.dashboard

import com.google.gson.JsonArray
import com.netDashboard.folder_tree.FolderTree.dashboardsFile
import com.netDashboard.globals.G.gson
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList.Companion.toTileType
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type

class Dashboards {

    companion object {
        fun getSaved(): MutableList<Dashboard> {

            return mutableListOf() //todo

            val list: MutableList<Dashboard> = mutableListOf()

            val jsonArray = try {
                gson.fromJson(FileReader(dashboardsFile), JsonArray::class.java)
            } catch (e: Exception) {
                JsonArray()
            }

            for (jsonElement in jsonArray) {

                val tilesJsonArray = jsonElement.asJsonObject.getAsJsonArray("tiles")
                val tiles: MutableList<Tile> = mutableListOf()
                for (tileJsonElement in tilesJsonArray) {
                    try {
                        tileJsonElement.asJsonObject["type"].asString.toTileType()?.let { type ->
                            tiles.add(gson.fromJson(tileJsonElement, type as Type))
                        }
                    } catch (e: Exception) {
                        throw e
                    }
                }

                jsonElement.asJsonObject.remove("tiles")

                val dashboard = try {
                    gson.fromJson(jsonElement, Dashboard::class.java)
                } catch (e: Exception) {
                    throw e
                }

                dashboard.tiles = tiles
                list.add(dashboard)
            }

            return list
        }

        fun MutableList<Dashboard>.save() {
            try {
                File(dashboardsFile).writeText(gson.toJson(this))
            } catch (e: Exception) {
                throw e
            }
        }

        fun MutableList<Dashboard>.byId(id: Long): Dashboard =
            this.find { it.id == id } ?: Dashboard("err")
    }
}