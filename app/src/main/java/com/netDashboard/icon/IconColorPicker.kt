package com.netDashboard.icon

import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider
import com.netDashboard.R
import com.netDashboard.G.getIconHSV
import com.netDashboard.G.setIconHSV
import com.netDashboard.G.theme
import com.netDashboard.recycler_view.RecyclerViewAdapter

class IconColorPicker : Icon() {

    override val layout = R.layout.item_icon_color_picker
    override val spanCount = -1

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
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

        h.value = getIconHSV()[0]
        s.value = getIconHSV()[1]
        v.value = if (theme.a.isDark) 1f else getIconHSV()[2]

        fun onHSVChange() {
            val hsv = floatArrayOf(h.value, s.value, v.value)
            val p = theme.a.getColorPallet(hsv, true)
            setIconHSV(hsv)
            (adapter as IconAdapter).onColorChange(hsv, p)
        }

        h.addOnChangeListener(Slider.OnChangeListener { _, _, _ ->
            onHSVChange()
        })

        s.addOnChangeListener(Slider.OnChangeListener { _, _, _ ->
            onHSVChange()
        })

        v.addOnChangeListener(Slider.OnChangeListener { _, _, _ ->
            onHSVChange()
        })
    }
}