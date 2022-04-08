package com.alteratom.dashboard

import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.alteratom.dashboard.FolderTree
import com.alteratom.R
import com.alteratom.dashboard.foreground_service.DaemonGroup
import com.alteratom.dashboard.G
import com.alteratom.dashboard.log.Log
import com.alteratom.dashboard.prepareSave
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.screenWidth
import com.alteratom.dashboard.tile.Tile
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.random.Random

@Suppress("UNUSED")
class Dashboard(var name: String = "", var isInvalid: Boolean = false) : com.alteratom.dashboard.recycler_view.RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    var log = com.alteratom.dashboard.log.Log() //check

    @JsonIgnore
    var dg: com.alteratom.dashboard.foreground_service.DaemonGroup? = null

    var tiles: MutableList<com.alteratom.dashboard.tile.Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    var mqttEnabled = true

    var mqttAddress = "tcp://"
    var mqttPort = 1883
    var mqttCred = false
    var mqttUserName = ""
    var mqttPass = ""
    var mqttClientId = kotlin.math.abs(Random.nextInt()).toString()
    val mqttURI
        get() = "$mqttAddress:$mqttPort"

    var bluetoothEnabled = false

    companion object {
        fun MutableList<Dashboard>.saveToFile(save: String = this.prepareSave()) {
            try {
                File(com.alteratom.dashboard.FolderTree.dashboardsFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        private fun getSaveFromFile() = try {
            FileReader(com.alteratom.dashboard.FolderTree.dashboardsFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): MutableList<Dashboard>? =
            try {
                com.alteratom.dashboard.G.mapper.readerForListOf(Dashboard::class.java).readValue(save)
            } catch (e: Exception) {
                null
            }
    }

    override fun onBindViewHolder(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((com.alteratom.dashboard.screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }
}