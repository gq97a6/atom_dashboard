package com.netDashboard.tile.types.slider

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.databinding.PopupSliderBinding
import com.netDashboard.globals.G.theme
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
    var dragCon = false

    var value: Int = 0
        set(value) {
            field = value
            displayValue = value
        }

    @JsonIgnore
    var displayValue: Int = 0
        set(value) {
            field = value
            val dp = holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            dp?.text = value.toString()

            setBackground(value, holder?.itemView?.findViewById(R.id.ts_background))
        }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        if (step == 0) step = 1
        displayValue = value

        holder.itemView.findViewById<TextView>(R.id.ts_tag)?.let {
            it.text = if (tag.isBlank()) "???" else tag
        }

        holder.itemView.findViewById<View>(R.id.ts_icon)?.setBackgroundResource(iconRes)
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        if (!dragCon) {
            val dialog = Dialog(adapter.context)

            dialog.setContentView(R.layout.popup_slider)
            val binding = PopupSliderBinding.bind(dialog.findViewById(R.id.ps_root))

            binding.psValue.text = value.toString()
            setBackground(value, binding.tsBackground)

            binding.psRoot.setOnTouchListener { _, event ->
                control(event, v.parent as View).let {
                    if (it.second) dialog.dismiss()
                    else {
                        setBackground(it.first, binding.tsBackground)
                        binding.psValue.text = it.first.toString()
                    }
                    return@setOnTouchListener true
                }
            }

            theme.apply(binding.root, adapter.context)
            dialog.show()
        }
    }

    override fun onTouch(v: View, e: MotionEvent) {
        super.onTouch(v, e)

        if (dragCon) {
            control(e, holder?.itemView).let {
                displayValue = if (it.second) value else it.first
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

    private fun control(e: MotionEvent, v: View?): Pair<Int, Boolean> {
        var p = 100f * (e.rawX - screenWidth * 0.2f) / (screenWidth * (0.8f - 0.2f))
        if (p < 0) p = 0f
        else if (p > 100) p = 100f

        val value = (from + p * (to - from) / 100).toInt().roundCloser(step)

        when (e.action) {
            ACTION_DOWN -> (v as ViewGroup?)?.requestDisallowInterceptTouchEvent(true)
            ACTION_UP -> {
                (v as ViewGroup?)?.requestDisallowInterceptTouchEvent(false)
                send(mqttData.pubPayload.replace("@value", value.toString()), mqttData.qos)
                return Pair(value, true)
            }
        }

        return Pair(value, false)
    }

    private fun setBackground(value: Int, background: View?) {
        val params = background?.layoutParams as ConstraintLayout.LayoutParams?
        params?.matchConstraintPercentWidth = abs((((from - value).toFloat() / (to - from))))
        background?.requestLayout()
    }
}