package com.netDashboard.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.main_settings.MainSettings
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TileTypeList
import java.io.*
import java.util.*

open class Dashboard(private val rootPath: String, val name: String) :
    Serializable {

    val id: Long?

    var context: Context? = null
    private var holder: DashboardAdapter.DashboardsViewHolder? = null

    private val rootFolder = "$rootPath/dashboard_data/$name"
    private val tilesFileName = "$rootFolder/tiles"
    private val settingsFileName = "$rootFolder/settings"

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

    init {
        id = Random().nextLong()
    }

    fun getItemViewType(context: Context): Int {
        this.context = context

        return R.layout.dashboard_list_element
    }

    open fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardAdapter.DashboardsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        view.findViewById<Button>(R.id.dle_button).text = name.uppercase(Locale.getDefault())

        view.findViewById<Button>(R.id.dle_button).setOnClickListener {

            Intent(context, DashboardActivity::class.java).also {
                val settings = MainSettings(rootPath).getSaved()
                settings.lastDashboardName = name
                settings.save()

                it.putExtra("dashboardName", name)
                (context as Activity).overridePendingTransition(0, 0)
                context?.startActivity(it)
            }
        }
        return DashboardAdapter.DashboardsViewHolder(view)
    }

    open fun onBindViewHolder(holder: DashboardAdapter.DashboardsViewHolder, position: Int) {
        this.holder = holder
    }

    fun areItemsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem.id == newItem.id
    }

    fun List<Tile>.save() {

        FolderTree(rootFolder).check()

        for ((i, _) in this.withIndex()) {
            this[i].context = null
            this[i].holder = null
            this[i].mqttd = null

            this[i].isEdit = false
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

    @Suppress("unused")
    private fun List<Tile>.getSaved(): List<Tile> {

        if (!FolderTree(rootFolder).check()) return TileTypeList().getTestDashboard()

        return try {
            val file = FileInputStream(tilesFileName)
            val inStream = ObjectInputStream(file)

            val list = inStream.readObject() as List<*>

            inStream.close()
            file.close()

            list.filterIsInstance<Tile>().takeIf { it.size == list.size } ?: listOf()
        } catch (e: Exception) {
            TileTypeList().getTestDashboard()
        }
    }

    inner class Settings : Serializable {
        var spanCount = 3

        //MQTT
        var mqttEnabled: Boolean = false
        var mqttAddress = "tcp://"
        var mqttPort = 1883
        val mqttURI
            get() = "$mqttAddress:$mqttPort"

        //Bluetooth
        var bluetoothEnabled: Boolean = false

        var dashboardTagName = name

        fun save() {

            FolderTree(rootFolder).check()

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

            if (!FolderTree(rootFolder).check()) return Settings()

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