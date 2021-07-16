package com.netDashboard.tile.types.slider

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.netDashboard.*
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TilesAdapter
import org.eclipse.paho.client.mqttv3.MqttMessage

class SliderTile : Tile() {

    @Transient
    override val layout = R.layout.tile_slider

    @Transient
    override val mqttDefaultPubValue = "@value"
    override var mqttPubValue = mqttDefaultPubValue

    init {
        name = "slider"
    }

    var value = 0f
    var from = 0f
    var to = 100f
    var step = 1f

    private var liveValue: Float
        get() {
            val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider)

            return when {
                from < to -> slider?.value ?: 0f
                to < from -> slider?.valueFrom ?: 0f - (slider?.value ?: 0f)
                else -> 0f
            }
        }
        set(value) {
            value.roundCloser(step).let {
                this.value = it
                val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider)

                when {
                    from < to -> slider?.value = it
                    to < from -> slider?.value = from - it
                }

                holder?.itemView?.findViewById<TextView>(R.id.ts_value)?.text = it.dezero()
            }
        }

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val slider = holder.itemView.findViewById<Slider>(R.id.ts_slider)
        val background = holder.itemView.findViewById<View>(R.id.background)

        slider.isEnabled = !isEdit
        setRange(from, to, step)

        background.setOnTouchListener { v, e ->

            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.performClick()
                MotionEvent.ACTION_UP -> holder.itemView.callOnClick()
            }

            if ((e.eventTime - e.downTime) > 0) {

                val params = slider.layoutParams as FrameLayout.LayoutParams
                params.width = getScreenWidth() - 100.toPx()
                slider.layoutParams = params

                val center = getScreenWidth() / 2
                val location = IntArray(2)
                background.getLocationOnScreen(location)
                val offset = center - location[0] - slider.width / 2

                e.setLocation(e.x - offset, e.y)

                slider.dispatchTouchEvent(e)
            }

            return@setOnTouchListener true
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(s: Slider) {
                holder.itemView.callOnClick()
            }

            override fun onStopTrackingTouch(s: Slider) {
                val topic = mqttTopics.pubs.get("base")
                onSend(topic.topic, mqttPubValue.replace("@value", liveValue.dezero()), topic.qos)
            }
        })

        slider.addOnChangeListener(Slider.OnChangeListener { _: Slider, value: Float, _: Boolean ->
            liveValue = value
        })

        setThemeColor(color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        holder?.itemView?.findViewById<View>(R.id.background)?.setBackgroundColor(color)

        holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            ?.setTextColor(getContrastColor(color).alpha(.75f))
    }

    private fun setRange(from: Float, to: Float, step: Float = 1f) {
        if (from == to || step !in 0.0000000001..1000000000.0) return
        val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider) ?: return

        val f = from.roundCloser(step)
        val t = to.roundCloser(step)

        if (from < to) {
            slider.valueFrom = f
            slider.valueTo = t
            slider.stepSize = step
        } else if (to < from) {
            slider.valueFrom = t
            slider.valueTo = f
            slider.stepSize = step
        }

        if (slider.value !in f..t) slider.value = f

        this.from = f
        this.to = t
        this.step = step

        liveValue = if (value in from..to) value else slider.valueFrom
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false

        val value = data.second.toString().toFloatOrNull()
        if (value != null) liveValue = value

        return true
    }
}