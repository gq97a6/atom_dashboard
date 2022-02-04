package com.netDashboard.icon

import android.view.KeyEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.netDashboard.R
import com.netDashboard.globals.G.theme
import com.netDashboard.globals.G.tile
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter


class IconColorPicker(
) : Icon() {

    override val layout = R.layout.item_icon_color_picker
    override val spanCount = -1
    var measuredHeight = 0

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        view.layoutParams = params

        val h = holder.itemView.findViewById<Slider>(R.id.iicp_hue)
        val s = holder.itemView.findViewById<Slider>(R.id.iicp_saturation)
        val v = holder.itemView.findViewById<Slider>(R.id.iicp_value)
        val vText = holder.itemView.findViewById<TextView>(R.id.iicp_val_text)
        val color = holder.itemView.findViewById<LinearLayout>(R.id.iicp_color)


        vText.tag = if (theme.a.isDark) "colorC" else "colorB"
        v.tag = if (theme.a.isDark) "disabled" else "enabled"
        v.isEnabled = !theme.a.isDark

        theme.apply(color, anim = false)

        h.value = tile.hsv[0]
        s.value = tile.hsv[1]
        v.value = if (theme.a.isDark) 1f else tile.hsv[2]

        fun onHSVChange() {
            val hsv = floatArrayOf(h.value, s.value, v.value)
            val p = theme.a.getColorPallet(hsv, true)
            tile.hsv = hsv
            (adapter as IconAdapter).onColorChange(hsv, p)
        }

        h.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            onHSVChange()
        })

        s.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            onHSVChange()
        })

        v.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            onHSVChange()
        })
    }
}