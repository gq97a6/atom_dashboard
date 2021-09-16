package com.netDashboard.dashboard

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.globals.G
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.theme.Theme
import com.netDashboard.tile.Tile
import java.util.*
import kotlin.random.Random

class Dashboard(var name: String = "") : BaseRecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    @Transient
    var daemonGroup: DaemonGroup? = null

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    var spanCount = 3

    var theme = Theme()

    var mqttEnabled = false

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

    companion object {
        fun MutableList<Dashboard>.byId(id: Long): Dashboard =
            this.find { it.id == id } ?: Dashboard("err")
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
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
        button?.backgroundTintList = ColorStateList.valueOf(G.theme.colorB)
        button?.setTextColor(G.theme.color)
    }
}