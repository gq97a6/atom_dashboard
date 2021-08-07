package com.netDashboard.dashboard

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.netDashboard.folder_tree.FolderTree.dashboardsFile
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList.Companion.toTileType
import java.io.File
import java.io.FileReader
import java.lang.reflect.Type

object Dashboards {

    private var isLive = false

    private val Gson = Gson()
    private var dashboards: MutableList<Dashboard> = mutableListOf()

    fun get(): MutableList<Dashboard> = dashboards
    fun get(n: String): Dashboard = dashboards.find { it.name == n } ?: Dashboard("err")
    fun add(d: Dashboard) {
        dashboards.add(d)
        save()
    }

    fun getSaved() {
        if (isLive) return

        val list: MutableList<Dashboard> = mutableListOf()

        val jsonArray = try {
            Gson.fromJson(FileReader(dashboardsFile), JsonArray::class.java)
        } catch (e: Exception) {
            JsonArray()
        }

        for (jsonElement in jsonArray) {

            val tilesJsonArray = jsonElement.asJsonObject.getAsJsonArray("tiles")
            val tiles: MutableList<Tile> = mutableListOf()
            for (tileJsonElement in tilesJsonArray) {
                try {
                    tileJsonElement.asJsonObject["type"].asString.toTileType()?.let { type ->
                        tiles.add(Gson.fromJson(tileJsonElement, type as Type))
                    }
                } catch (e: Exception) {
                    throw e
                }
            }

            jsonElement.asJsonObject.remove("tiles")

            val dashboard = try {
                Gson.fromJson(jsonElement, Dashboard::class.java)
            } catch (e: Exception) {
                throw e
            }

            dashboard.tiles = tiles
            list.add(dashboard)
        }

        dashboards = list
        isLive = true
    }

    fun save() {
        try {
            File(dashboardsFile).writeText(Gson.toJson(dashboards))
        } catch (e: Exception) {
            throw e
        }
    }
}