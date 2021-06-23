package com.netDashboard.tile.types.slider

import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.netDashboard.*
import com.netDashboard.tile.Tile
import com.netDashboard.tile.TilesAdapter

class SliderTile(name: String, color: Int, width: Int, height: Int) :
    Tile("slider", name, color, R.layout.tile_slider, width, height) {

    private var value = 50f
    private var from = 0f
    private var to = 100f
    private var step = 10f

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val slider = holder.itemView.findViewById<Slider>(R.id.ts_slider)
        val background = holder.itemView.findViewById<View>(R.id.background)

        slider.isEnabled = !isEdit
        setRange(from, to, step)
        slider.value = value

        //Display value
        holder.itemView.findViewById<TextView>(R.id.ts_value).text = value.toString()

        //Use background of tile as slider input
        background.setOnTouchListener { v, e ->

            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.performClick()
            }

            if ((e.eventTime - e.downTime) > 0) {

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
                e.setLocation(e.x - offset, e.y)

                //Push event
                slider.dispatchTouchEvent(e)
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
                value = s.value
            }
        })

        slider.addOnChangeListener(Slider.OnChangeListener { _: Slider, value: Float, _: Boolean ->
            holder.itemView.findViewById<TextView>(R.id.ts_value).text = value.toString()
        })

        setThemeColor(color)
    }

    override fun onClick() {
    }

    override fun onEdit(isEdit: Boolean) {
        super.onEdit(isEdit)

        holder?.itemView?.findViewById<Slider>(R.id.ts_slider)?.isEnabled = !isEdit
    }

    override fun setThemeColor(color: Int) {
        super.setThemeColor(color)

        holder?.itemView?.findViewById<View>(R.id.background)?.setBackgroundColor(color)

        holder?.itemView?.findViewById<TextView>(R.id.ts_value)
            ?.setTextColor(getContrastColor(color).alpha(75))
    }

    private fun setRange(from: Float, to: Float, step: Float = 1f) {
        val slider = holder?.itemView?.findViewById<Slider>(R.id.ts_slider)

        if (from < to && slider != null) {
            slider.valueFrom = from
            slider.valueTo = to
            slider.stepSize = step

            if (value !in from..to) {
                slider.value = from
            }
        }
    }

    override fun onLock(isLocked: Boolean) {
        super.onLock(isLocked)

        holder?.itemView?.findViewById<Slider>(R.id.ts_slider)?.isEnabled = !isLocked
    }
}