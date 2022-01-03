package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile

class ButtonTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_button

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<TextView>(R.id.tb_tag)?.let {
            if (tag.isBlank()) it.visibility = GONE
            else it.text = tag
        }

        holder.itemView.findViewById<View>(R.id.view11)?.setBackgroundResource(icon.res)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        send(mqttData.pubPayload, mqttData.qos)
    }
}