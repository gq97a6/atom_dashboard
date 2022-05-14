package com.alteratom.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import com.alteratom.R
import com.fasterxml.jackson.annotation.JsonIgnore

class ButtonTile : com.alteratom.dashboard.tile.Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_button

    @JsonIgnore
    override var typeTag = "button"

    override var iconKey = "il_arrow_arrow_to_bottom"

    override fun onBindViewHolder(holder: com.alteratom.dashboard.recycler_view.RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility = GONE
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        send(mqtt.payloads["base"] ?: "")
    }
}