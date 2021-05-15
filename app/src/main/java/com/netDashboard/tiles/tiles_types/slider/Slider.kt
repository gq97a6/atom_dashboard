package com.netDashboard.tiles.tiles_types.slider

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import com.google.android.material.slider.Slider
import com.netDashboard.*
import com.netDashboard.abyss.Abyss
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter

class SliderTile(name: String, color: Int, width: Int, height: Int) :
    Tile(name, color, R.layout.slider_tile, width, height) {

    private var value = 50f
    private var from = 0f
    private var to = 100f
    private var step = 10f

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val slider = holder.itemView.findViewById<Slider>(R.id.slider)
        val background = holder.itemView.findViewById<View>(R.id.background)

        slider.isEnabled = !editMode()
        setRange(from, to, step)
        slider.value = value

        //Display value
        holder.itemView.findViewById<TextView>(R.id.slider_value).text = value.toString()

        //Use background of tile as slider input
        background.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> view.performClick()
            }

            if ((event.eventTime - event.downTime) > 0) {

                //Make slider width equal to screen width
                val params = slider.layoutParams as FrameLayout.LayoutParams
                params.width = getScreenWidth() - 100.toPx()
                slider.layoutParams = params

                //Get offset
                val center = getScreenWidth() / 2
                val location = IntArray(2)
                background.getLocationOnScreen(location)
                val offset = center - location[0] - slider.width / 2

                //Apply offset to event
                event.setLocation(event.x - offset, event.y)

                //Push event
                slider.dispatchTouchEvent(event)
            }

            if ((event.eventTime - event.downTime) > 1000 && event.action == MotionEvent.ACTION_UP) {
                //Abyss().udpd.send(value.toString(), "192.168.0.18", 5452)
            }

            return@setOnTouchListener true
        }

        //Interpret click on background as click on tile
        background.setOnClickListener {
            holder.itemView.callOnClick()
        }

        //Interpret click on slider as click on tile
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(s: Slider) {
                holder.itemView.callOnClick()
            }

            override fun onStopTrackingTouch(s: Slider) {
            }
        })

        slider.addOnChangeListener(Slider.OnChangeListener { _: Slider, value: Float, _: Boolean ->

            this.value = slider.value
            holder.itemView.findViewById<TextView>(R.id.slider_value).text = value.toString()
        })

        setThemeColor(color)
    }

    override fun onClick() {
        if (!editMode()) {
            setThemeColor(getRandomColor())
        }
    }

    override fun editMode(isEnabled: Boolean) {
        super.editMode(isEnabled)

        holder?.itemView?.findViewById<Slider>(R.id.slider)?.isEnabled = !editMode()
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        holder?.itemView?.findViewById<View>(R.id.background)?.setBackgroundColor(color)

        holder?.itemView?.findViewById<TextView>(R.id.slider_value)
            ?.setTextColor(getContrastColor(color).alpha(75))
    }

    private fun setRange(from: Float, to: Float, step: Float = 1f) {
        val slider = holder?.itemView?.findViewById<Slider>(R.id.slider)

        if (from < to && slider != null) {
            slider.valueFrom = from
            slider.valueTo = to
            slider.stepSize = step

            if (value !in from..to) {
                slider.value = from
            }
        }
    }

    override fun onData(data: String) {
        super.onData(data)

        val numData = data.toIntOrNull() ?: 0
        value = numData.toFloat()
        holder?.itemView?.findViewById<TextView>(R.id.slider_value)?.text = value.toString()
    }
}