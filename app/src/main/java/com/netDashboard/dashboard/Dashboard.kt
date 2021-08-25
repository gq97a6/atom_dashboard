package com.netDashboard.dashboard

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import com.netDashboard.themes.Theme
import com.netDashboard.tile.Tile
import java.util.*

class Dashboard(var name: String = "") : RecyclerViewItem() {

    var isDeprecated = false

    override val layout
        get() = R.layout.dashboard_list_item

    @Transient
    var daemonGroup: DaemonGroup? = null

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboardId = id
            field = value
        }

    var spanCount = 3

    var mqttEnabled: Boolean = false
        get() = field && !isDeprecated

    var mqttAddress = "tcp://"
    var mqttPort = 1883
    val mqttURI
        get() = "$mqttAddress:$mqttPort"

    var bluetoothEnabled: Boolean = false

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Button>(R.id.dle_button).text =
            name.uppercase(Locale.getDefault())

        holder.itemView.findViewById<Button>(R.id.dle_button).setOnClickListener {
            holder.itemView.callOnClick()
        }

        applyTheme()
    }

    private fun applyTheme() {
        val button = holder?.itemView?.findViewById<Button>(R.id.dle_button)
        button?.backgroundTintList = ColorStateList.valueOf(Theme.colorB)
        button?.setTextColor(Theme.color)
    }
}