package com.alteratom.dashboard.dashboard

import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.foreground_service.DaemonsManager
import com.alteratom.dashboard.log.Log
import com.alteratom.dashboard.screenWidth
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import kotlin.reflect.KClass

@Suppress("UNUSED")
class Dashboard(var name: String = "", var isInvalid: Boolean = false) :
    com.alteratom.dashboard.recycler_view.RecyclerViewItem() {

    override val layout
        get() = R.layout.item_dashboard

    var log = Log()

    @JsonIgnore
    lateinit var dg: DaemonsManager.DaemonGroup
    var daemonsIds = mutableMapOf<KClass<out Any>, Long>()

    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboard = this
            field = value
        }

    override fun onBindViewHolder(
        holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((screenWidth - view.paddingLeft * 2) * 1 / 3.236).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<TextView>(R.id.id_tag).text =
            name.uppercase(Locale.getDefault())
    }
}