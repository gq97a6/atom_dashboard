package com.netDashboard.tile.types.button

import android.content.res.ColorStateList
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.getContrastColor
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TilesAdapter
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*
import kotlin.concurrent.timerTask

class ButtonTile : Tile() {

    override val layout: Int
        get() = R.layout.tile_button

    @Transient
    override val mqttDefaultPubValue = "1"
    override var mqttPubValue = mqttDefaultPubValue

    init {
        name = "button"
    }

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
        setThemeColor(color)
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

        val topic = mqttTopics.pubs.get("base")
        onSend(topic.topic, mqttPubValue, topic.qos)

        holder?.itemView?.animate()
            ?.alpha(0f)
            ?.withEndAction {
                holder?.itemView?.animate()?.alpha(1f)
                    ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
            }
            ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400

        width = if (width == 1) spanCount else 1
        adapter?.notifyItemChanged(holder?.adapterPosition!!)

        val test = timerTask {
        }

        Timer().schedule(test, 1000, 800)
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false
        liveText = data.second.toString()
        return true
    }
}