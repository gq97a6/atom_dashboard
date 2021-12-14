package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class TextTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_text

    override val mqttData = MqttData("1")

    @JsonIgnore
    override var typeTag = "text"

    var value = ""
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tt_value)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        value = value

        holder.itemView.findViewById<TextView>(R.id.tt_tag)?.let {
            it.text = if (tag.isBlank()) "???" else tag
        }
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        send(mqttData.pubPayload, mqttData.qos)
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        value = jsonResult["value"] ?: data.second.toString()
    }
}