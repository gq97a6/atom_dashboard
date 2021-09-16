package com.netDashboard.tile.types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.globals.G.theme
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

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

        holder.itemView.findViewById<Button>(R.id.tb_button).setOnClickListener {
            holder.itemView.callOnClick()
        }

        liveText = text
    }

    override fun applyTheme() {
        super.applyTheme()

        val theme = if (dashboard.theme.useOver) dashboard.theme else theme

        val button = holder?.itemView?.findViewById<Button>(R.id.tb_button)
        button?.backgroundTintList = ColorStateList.valueOf(theme.colorB)
        button?.setTextColor(theme.color)
    }


    override fun onClick() {
        super.onClick()

        val topic = mqttTopics.pubs.get("base")
        onSend(topic.topic, mqttPubValue, topic.qos)

        //adapter?.spanCount?.let { sp ->
        //    holder?.adapterPosition?.let { ap ->
        //        width = if (width == 1) sp else 1
        //        adapter?.notifyItemChanged(ap)
        //    }
        //}
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false
        liveText = data.second.toString()
        return true
    }
}