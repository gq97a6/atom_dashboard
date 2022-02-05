package com.netDashboard.icon

import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import com.netDashboard.R
import com.netDashboard.globals.G.theme
import com.netDashboard.globals.G.tile
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconIcon(
    val res: Int = 0,
    val type: String = "",
    val cat: String = ""
) : Icon() {

    override val layout = R.layout.item_icon_icon

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView

        val icon = view.findViewById<View>(R.id.iii_icon)
        icon.setBackgroundResource(res)
        icon.backgroundTintList = ColorStateList.valueOf(theme.a.colorPallet.a)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        tile.iconRes = res
        (adapter as IconAdapter).onIconChange(res)
    }
}