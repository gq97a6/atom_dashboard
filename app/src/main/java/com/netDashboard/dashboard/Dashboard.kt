package com.netDashboard.dashboard

import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.FolderTree
import com.netDashboard.R
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.G
import com.netDashboard.log.Log
import com.netDashboard.prepareSave
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import com.netDashboard.screenWidth
import com.netDashboard.tile.Tile
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.random.Random

@Suppress("UNUSED")
class Dashboard(var name: String = "", var isInvalid: Boolean = false) : RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    var log = Log() //check

    @JsonIgnore
    var dg: DaemonGroup? = null

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    var mqttEnabled = true

    var mqttAddress = "tcp://"
    var mqttPort = 1883
    var mqttUserName = ""
    var mqttPass = ""
    var mqttClientId = kotlin.math.abs(Random.nextInt()).toString()
    val mqttURI
        get() = "$mqttAddress:$mqttPort"

    var bluetoothEnabled = false

    companion object {
        fun MutableList<Dashboard>.saveToFile(save: String = this.prepareSave()) {
            try {
                File(FolderTree.dashboardsFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        private fun getSaveFromFile() = try {
            FileReader(FolderTree.dashboardsFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): MutableList<Dashboard>? =
            try {
                G.mapper.readerForListOf(Dashboard::class.java).readValue(save)
            } catch (e: Exception) {
                null
            }
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }
}