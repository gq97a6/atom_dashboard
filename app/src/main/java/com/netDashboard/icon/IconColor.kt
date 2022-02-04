package com.netDashboard.icon

import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import com.netDashboard.R
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.globals.G.tile
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem

class IconColor(
    var hsv: FloatArray = floatArrayOf(),
    var colorPallet: ColorPallet = ColorPallet(0, 0, 0, 0, 0, 0)
) : Icon() {

    override val layout = R.layout.item_icon_color

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val color = holder.itemView.findViewById<View>(R.id.iic_color)
        color.backgroundTintList = ColorStateList.valueOf(colorPallet.color)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        tile.hsv = hsv
        (adapter as IconAdapter).setColorPicker(hsv)
        (adapter as IconAdapter).onColorChange(hsv, colorPallet)
    }
}