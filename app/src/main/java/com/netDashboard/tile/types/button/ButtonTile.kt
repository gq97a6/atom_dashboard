package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile

class ButtonTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_button

    @JsonIgnore
    override var typeTag = "button"

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility = GONE
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        send(mqttData.payloads["base"] ?: "", mqttData.qos)
    }
}