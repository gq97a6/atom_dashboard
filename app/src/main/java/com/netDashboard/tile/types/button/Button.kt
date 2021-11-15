package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.MqttData
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ButtonTile : Tile() {

    @Transient
    override val layout = R.layout.tile_button

    override val mqttData = MqttData("1")

    @Transient
    override var typeTag = "button"

    var value = "Default value"
        set(value) {
            field = value
            holder?.itemView?.findViewById<TextView>(R.id.tb_value)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        value = value
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        onPublish(mqttData.pubPayload, mqttData.qos)
    }

    override fun onReceive(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onReceive(data)) return false
        value = data.second.toString()
        return true
    }
}