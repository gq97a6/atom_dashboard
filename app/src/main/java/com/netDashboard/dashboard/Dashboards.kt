package com.netDashboard.dashboard

import android.util.Log
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.dashboardsFolder
import com.netDashboard.folder_tree.FolderTree.save
import java.io.File

object Dashboards {

    lateinit var list: MutableList<Dashboard>

    fun set() {

        val list: MutableList<Dashboard> = mutableListOf()

        for (name in getNames()) {

            val dashboard = try {
                val fileName = FolderTree.dashboardFile(name)
                FolderTree.getSaved(Dashboard::class.java, fileName) as Dashboard
            } catch (e: Exception) {
                Log.i("OUY", e.toString())
                Dashboard(name)
            }

            list.add(dashboard)
        }

        //@SerializedName("description")
        //val fileName = FolderTree.tilesPropertiesFile("test")
        //Dashboard("").save(fileName)
        //val dashboard = FolderTree.getSaved(Dashboard::class.java, fileName) as Dashboard

        this.list = list
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

    fun get(name: String): Dashboard? {
        for (d in list) {
            if (d.name == name) return d
        }

        return null
    }

    private fun Dashboard.save() {
        try {
            val fileName = FolderTree.dashboardFile(this.name)
            this.save(fileName)
        } catch (e: java.lang.Exception) {
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