package com.netDashboard.dashboard

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
    private var dashboards: MutableMap<String, Dashboard> = mutableMapOf()

    fun get(): MutableList<Dashboard> = dashboards.values.toMutableList()
    fun get(name: String): Dashboard? = dashboards[name]

    fun getSaved() {
        if (isLive) return

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames()) {

            val dashboard = try {
                val fileName = FolderTree.dashboardFile(name)
                Gson.fromJson(FileReader(fileName), Dashboard::class.java)
            } catch (e: Exception) {
                Dashboard(name)
            }

            val jsonArray = try {
                val fileName = FolderTree.tilesFile(name)
                Gson.fromJson(FileReader(fileName), JsonArray::class.java)
            } catch (e: Exception) {
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
            }

            dashboard.tiles = tiles
            list.add(dashboard)
        }

        for (d in list) dashboards[d.name] = d
        isLive = true
    }

    fun save() {
        for (d in dashboards.values) d.save()
    }

    fun save(name: String) = dashboards[name]?.save()

    private fun Dashboard.save() {
        try {
            File(FolderTree.dashboardFile(this.name))
                .writeText(Gson.toJson(this))

            File(FolderTree.tilesFile(this.name))
                .writeText(Gson.toJson(this.tiles))
        } catch (e: Exception) {
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