package com.netDashboard.tile.types.slider

import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.roundCloser
import com.netDashboard.screenWidth
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs

class SliderTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_slider

    override val mqttData = MqttData("@value")

    @JsonIgnore
    override var typeTag = "slider"

    var from = 0
    var to = 100
    var step = 1

    var value: Int = 0
        set(value) {
            val displayValue = holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            displayValue?.text = value.toString()
            field = value

            val background = holder?.itemView?.findViewById<View>(R.id.ts_background)
            val params = background?.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = abs((((from - value).toFloat() / (to - from))))
            background.requestLayout()
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (step == 0) step = 1
        value = value

        holder.itemView.findViewById<TextView>(R.id.ts_tag)?.let {
            if (tag.isBlank()) it.visibility = View.GONE
            else it.text = tag
        }
    }

    override fun onTouch(v: View, e: MotionEvent) {
        super.onTouch(v, e)

        var p = 100f * (e.rawX - screenWidth * 0.2f) / (screenWidth * (0.8f - 0.2f))
        if (p < 0) p = 0f
        else if (p > 100) p = 100f

        value = (from + p * (to - from) / 100).toInt().roundCloser(step)

        when (e.action) {
            ACTION_DOWN -> (holder?.itemView as ViewGroup).requestDisallowInterceptTouchEvent(
                true
            )
            ACTION_UP -> {
                (holder?.itemView as ViewGroup).requestDisallowInterceptTouchEvent(false)
                send(mqttData.pubPayload.replace("@value", value.toString()), mqttData.qos)
            }
        }
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        (jsonResult["value"] ?: data.second.toString()).toIntOrNull()
            ?.let { this.value = it.roundCloser(step) }
    }
}