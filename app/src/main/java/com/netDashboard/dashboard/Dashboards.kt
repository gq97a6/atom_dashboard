package com.netDashboard.dashboard

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.dashboardsFolder
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList.Companion.toTileType
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type

object Dashboards {

    private var isLive = false

    private val Gson = Gson()
    var list: MutableList<Dashboard> = mutableListOf()

    fun get(name: String): Dashboard? {
        for (d in list) {
            if (d.name == name) return d
        }
        return null
    }

    fun getSaved() {
        if(isLive) return

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames()) {

            val dashboard = try {
                val fileName = FolderTree.dashboardFile(name)
                Gson.fromJson(FileReader(fileName), Dashboard::class.java)
            } catch (e: Exception) {
                Log.i("OUY", "Dashboards.getSaved: $e")
                Dashboard(name)
            }

            val jsonArray = try {
                val fileName = FolderTree.tilesFile(name)
                Gson.fromJson(FileReader(fileName), JsonArray::class.java)
            } catch (e: Exception) {
                Log.i("OUY", "Dashboards.getSaved: $e")
                JsonArray()
            }

            val tiles: MutableList<Tile> = mutableListOf()
            try {
                for (jsonElement in jsonArray) {
                    val type = jsonElement.asJsonObject["type"].asString.toTileType()
                    if (type != null) {
                        tiles.add(
                            Gson.fromJson(
                                jsonElement,
                                type as Type
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.i("OUY", "Dashboards.getSaved: $e")
            }

            dashboard.tiles = tiles
            list.add(dashboard)
        }

        this.list = list
        isLive = true
    }

    fun save() {
        for (d in list) d.save()
    }

    fun save(name: String) {
        for (d in list) {
            if (d.name != name) continue
            d.save()
            if (d.name == name) break
        }
    }

    private fun Dashboard.save() {
        try {
            File(FolderTree.dashboardFile(this.name))
                .writeText(Gson.toJson(this))

            File(FolderTree.tilesFile(this.name))
                .writeText(Gson.toJson(this.tiles))
        } catch (e: Exception) {
            Log.i("OUY", "Dashboard.save: $e")
        }
    }

    private fun getNames(): List<String> {
        val list: MutableList<String> = mutableListOf()
        File(dashboardsFolder).list()?.forEach {
            list.add(it.toString().substringAfterLast('/'))
        }
        return list
    }
}