package com.netDashboard.dashboard

import android.util.Log
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.dashboardsFolder
import com.netDashboard.folder_tree.FolderTree.save
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.slider.SliderTile
import java.io.File

object Dashboards {

    lateinit var list: MutableList<Dashboard>

    fun get(name: String): Dashboard? {
        for (d in list) {
            if (d.name == name) return d
        }

        return null
    }

    fun set() {

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames()) {
            val dashboard = Dashboard(name)
            val tiles: MutableList<Tile> = mutableListOf()

            try {
                dashboard.p = FolderTree.getSaved(
                    Dashboard.Properties::class.java,
                    FolderTree.dashboardPropertiesFile(name)
                ) as Dashboard.Properties
            } catch (e: Exception) {
                Log.i("OUY", "No dashboard properties save.")
            }

            val tilesPropertiesList = try {
                FolderTree.getSaved(
                    Tile.PropertiesList::class.java,
                    FolderTree.tilesPropertiesFile(name)
                ) as Tile.PropertiesList
            } catch (e: Exception) {
                Log.i("OUY", "No tiles properties save.")
                Tile.PropertiesList()
            }

            for (p in tilesPropertiesList.list) {
                val tile = when (p.type) {
                    "button" -> ButtonTile()
                    "slider" -> SliderTile()
                    else -> ButtonTile()
                }

                tile.p = p
                tiles.add(tile)
            }

            dashboard.tiles = tiles
            list.add(dashboard)
        }

        //@SerializedName("description")
        //val fileName = FolderTree.tilesPropertiesFile("test")
        //Dashboard("").save(fileName)
        //val dashboard = FolderTree.getSaved(Dashboard::class.java, fileName) as Dashboard

        this.list = list
    }

    fun save(name: String) {
        for (d in list) {
            if (d.name != name) continue

            val tilesPropertiesList = Tile.PropertiesList()

            for (t in d.tiles) {
                tilesPropertiesList.list.add(t.p)
            }

            d.p.save(FolderTree.dashboardPropertiesFile(d.name))
            tilesPropertiesList.save(FolderTree.tilesPropertiesFile(d.name))

            if (d.name == name) break
        }
    }

    fun save() {
        for (d in list) {
            val tilesPropertiesList = Tile.PropertiesList()

            for (t in d.tiles) {
                tilesPropertiesList.list.add(t.p)
            }

            d.p.save(FolderTree.dashboardPropertiesFile(d.name))
            tilesPropertiesList.save(FolderTree.tilesPropertiesFile(d.name))
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