package com.netDashboard.dashboard

import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.log.Log
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.tile.Tile
import java.util.*
import kotlin.random.Random

@Suppress("UNUSED")
class Dashboard(var name: String = "", var isInvalid: Boolean = false) : BaseRecyclerViewItem() {

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
    var mqttUserName: String? = ""
        get() = if ((field ?: "").isBlank()) null else field
    var mqttPass: String? = ""
        get() = if ((field ?: "").isBlank()) null else field
    var mqttClientId: String = kotlin.math.abs(Random.nextInt()).toString()
    val mqttURI
        get() = "$mqttAddress:$mqttPort"

    var bluetoothEnabled = false

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }
}