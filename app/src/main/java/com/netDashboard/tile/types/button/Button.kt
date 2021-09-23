package com.netDashboard.tile.types.button

import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.log.Log.Companion.LogList
import com.netDashboard.log.LogEntry
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.random.Random

class ButtonTile : Tile() {

    @Transient
    override val layout = R.layout.tile_button

    @Transient
    override val mqttDefaultPubValue = "1"
    override var mqttPubValue = mqttDefaultPubValue

    @Transient
    override var typeTag = "button"

    var text = "Default value"
    private var liveText: String
        get() = holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text.toString()
        set(value) {
            text = value
            holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text = value
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        liveText = text
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        //TODO
        LogList.add(LogEntry("00:20", "01.01.20", kotlin.math.abs(Random.nextInt()).toString()))
        val topic = mqttTopics.pubs.get("base")
        onSend(topic.topic, mqttPubValue, topic.qos)
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false
        liveText = data.second.toString()
        return true
    }
}