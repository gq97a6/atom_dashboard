package com.netDashboard.tile.types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.getContrastColor
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TilesAdapter
import org.eclipse.paho.client.mqttv3.MqttMessage

class ButtonTile(name: String, color: Int, width: Int, height: Int) :
    Tile(name, color, R.layout.tile_button, width, height) {

    var text = "Default value"
    var liveText: String
        get() = holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text.toString()
        set(value) {
            text = value
            holder?.itemView?.findViewById<Button>(R.id.tb_button)?.text = value
        }

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Button>(R.id.tb_button).setOnClickListener {
            holder.itemView.callOnClick()
        }

        holder.itemView.findViewById<Button>(R.id.tb_button).text = text

        liveText = text
        setThemeColor(p.color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        val button = holder?.itemView?.findViewById<Button>(R.id.tb_button)

        button?.backgroundTintList =
            ColorStateList.valueOf(
                color
            )

        button?.setTextColor(getContrastColor(color).alpha(.75f))
    }

    override fun onClick() {
        super.onClick()

        onSend(p.mqttTopics.pub.get("pub") ?: "", "0")
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false

        var counter = liveText.toIntOrNull() ?: 0
        counter++
        liveText = counter.toString()

        return true
    }
}