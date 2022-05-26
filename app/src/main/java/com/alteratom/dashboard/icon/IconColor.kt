package com.alteratom.dashboard.icon

import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import com.alteratom.R
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter

class IconColor(
    override val spanCount: Int,
    private var hsv: FloatArray = floatArrayOf(),
    private var colorPallet: ColorPallet = ColorPallet(0, 0, 0, 0, 0, 0)
) : Icon() {
    override val layout = R.layout.item_icon_color

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val color = holder.itemView.findViewById<View>(R.id.iic_color)
        color.backgroundTintList = ColorStateList.valueOf(colorPallet.color)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        setIconHSV(hsv)
        (adapter as IconAdapter).onColorChange(hsv, colorPallet)
    }
}