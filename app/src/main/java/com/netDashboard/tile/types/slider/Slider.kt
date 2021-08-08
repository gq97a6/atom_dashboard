package com.netDashboard.tile.types.slider

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.netDashboard.*
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs

class SliderTile : Tile() {

    @Transient
    override val layout = R.layout.tile_slider

    @Transient
    override val mqttDefaultPubValue = "@value"
    override var mqttPubValue = mqttDefaultPubValue

    @Transient
    override var name = "slider"

    var from = 0f
    var to = 100f
    var step = 1f

    private var _value = 0f
        set(value) {
            val displayValue = holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            displayValue?.text = value.toString()
            field = value
        }

    var value: Float
        set(value) {
            val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider)
            var v = value

            if (value !in from..to && value !in to..from) {
                v = if (abs(from - value) < abs(to - value)) from else to
            }

            v.roundCloser(step).let {
                slider?.value = it.checkScale()
                _value = it
            }
        }
        get() = _value

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val slider = holder.itemView.findViewById<Slider>(R.id.ts_slider)
        val background = holder.itemView.findViewById<View>(R.id.background)

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
                onSend(
                    topic.topic,
                    mqttPubValue.replace("@value", value.dezero()),
                    topic.qos
                )
            }
        })

        slider.addOnChangeListener(Slider.OnChangeListener { _: Slider, value: Float, _: Boolean ->
            this._value = value.roundCloser(step).checkScale()
        })

        setThemeColor(color)
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        holder?.itemView?.findViewById<View>(R.id.background)?.setBackgroundColor(color)

        holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            ?.setTextColor(getContrastColor(color).alpha(.75f))
    }

    override fun onEdit(isEdit: Boolean) {
        super.onEdit(isEdit)

        val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider)
        slider?.isEnabled = !isEdit
    }

    private fun Float.checkScale(): Float {
        return if (from < to) this else from - this + to
    }

    private fun setRange(from: Float, to: Float, step: Float = 1f) {
        val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider) ?: return

        val s = if (step in 0.000001..1000000000.0) step else SliderTile().step
        var f = from.roundCloser(s)
        var t = to.roundCloser(s)

        if (f == t) {
            f = SliderTile().from.roundCloser(s)
            t = SliderTile().to.roundCloser(s)
        }

        if (f < t) {
            slider.valueFrom = f
            slider.valueTo = t
        } else {
            slider.valueFrom = t
            slider.valueTo = f
        }
        slider.stepSize = s

        this.from = f
        this.to = t
        this.step = s
        value = _value
    }

    override fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!super.onData(data)) return false

        val value = data.second.toString().toFloatOrNull()
        if (value != null) this.value = value.roundCloser(step)

        return true
    }

}