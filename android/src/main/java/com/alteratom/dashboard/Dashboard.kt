package com.alteratom.dashboard

import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.daemon.daemons.mqttd.MqttConfig
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.log.Log
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Locale

open class Dashboard(var name: String = "", var type: Daemon.Type = Daemon.Type.MQTTD) :
    RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    var iconKey = "il_interface_plus_circle"
    val iconRes: Int
        get() = Icons.icons[iconKey]?.res ?: R.drawable.il_interface_plus_circle

    var hsv = aps.theme.a.hsv.let {
        floatArrayOf(it[0], it[1], it[2])
    }

    val pallet: Theme.ColorPallet
        get() = aps.theme.a.getColorPallet(hsv, true)

    var excludeNavigation = false
    var securityLevel = 0
    var log = Log()

    //Configuration for each daemon
    @JsonAlias("mqttData")
    var mqtt = MqttConfig()

    @JsonIgnore
    var daemon: Daemon? = null

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    override fun onBindViewHolder(
        holder: RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<View>(R.id.id_icon)?.setBackgroundResource(iconRes)
        
        if (!aps.isLicensed && position > 1) {
            holder.itemView.alpha = .5f
            holder.itemView.findViewById<TextView>(R.id.id_pro).visibility = VISIBLE
        }

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        aps.theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = pallet
        )
    }
}