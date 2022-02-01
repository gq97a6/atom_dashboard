package com.netDashboard.icon

import android.content.res.ColorStateList
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.netDashboard.R
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.globals.G.theme
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth

class IconPropertiesDrawable(
    val res: Int = 0,
    val type: String = "",
    val cat: String = "",
    val isCategory: Boolean = false,
    var hsv: FloatArray = floatArrayOf(),
    var colorPallet: ColorPallet = ColorPallet(0, 0, 0, 0, 0, 0),
    val isColor: Boolean = false
) :
    BaseRecyclerViewItem() {

    override val layout = R.layout.item_icon

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = (screenWidth - view.paddingLeft * 2) / adapter.spanCount
        view.layoutParams = params

        val icon = view.findViewById<View>(R.id.ic_icon)
        val text = view.findViewById<TextView>(R.id.ic_text)
        val frame = view.findViewById<View>(R.id.ic_frame)

        if (isCategory) {
            text.text = cat
            text.visibility = VISIBLE
            icon.visibility = GONE
            frame.visibility = VISIBLE
        } else if (isColor) {
            icon.backgroundTintList = ColorStateList.valueOf(colorPallet!!.color)
            icon.visibility = VISIBLE
            text.visibility = GONE
            frame.visibility = GONE
        } else {
            icon.setBackgroundResource(res)
            icon.backgroundTintList = ColorStateList.valueOf(theme.a.colorPallet.a)
            icon.visibility = VISIBLE
            text.visibility = GONE
            frame.visibility = GONE
        }
    }
}