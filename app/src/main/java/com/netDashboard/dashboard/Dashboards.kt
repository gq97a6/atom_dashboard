package com.netDashboard.dashboard

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.dashboardFile
import com.netDashboard.folder_tree.FolderTree.dashboardFolder
import com.netDashboard.folder_tree.FolderTree.dashboardRootFolder
import com.netDashboard.folder_tree.FolderTree.tilesFile
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
        FolderTree.buildPath(dashboardFolder(d.name))
        d.save()
    }

    fun getSaved() {
        if (isLive) return

        val list: MutableList<Dashboard> = mutableListOf()

        for (n in getNames()) {

            val dashboard = try {
                val fileName = dashboardFile(n)
                //Log.i("OUY", FileReader(fileName).readText())
                Gson.fromJson(FileReader(fileName), Dashboard::class.java)
            } catch (e: Exception) {
                Dashboard(n)
            }

            //"temporary" fix
            dashboard.setFlag()

            val jsonArray = try {
                val fileName = tilesFile(n)
                Gson.fromJson(FileReader(fileName), JsonArray::class.java)
            } catch (e: Exception) {
                JsonArray()
            }

            val tiles: MutableList<Tile> = mutableListOf()
            try {
                for (jsonElement in jsonArray) {
                    jsonElement.asJsonObject["type"].asString.toTileType()?.let { type ->
                        tiles.add(Gson.fromJson(jsonElement, type as Type))
                    }
                }
            } catch (e: Exception) {
            }

            dashboard.tiles = tiles
            list.add(dashboard)
        }

        dashboards = list
        isLive = true
    }

    fun save() {
        for (d in dashboards) d.save()
    }

    fun save(n: String) = dashboards.find { it.name == n }?.save()

    private fun Dashboard.save() {
        try {
            File(dashboardFile(this.name))
                .writeText(Gson.toJson(this))

            File(tilesFile(this.name))
                .writeText(Gson.toJson(this.tiles))
        } catch (e: Exception) {
            throw e
        }
    }

    private fun getNames(): List<String> {
        val list: MutableList<String> = mutableListOf()
        File(dashboardRootFolder).list()?.forEach {
            list.add(it.toString().substringAfterLast('/'))
        }

        return list
    }
}