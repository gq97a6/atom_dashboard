package com.alteratom.dashboard.icon

import android.content.res.ColorStateList
import android.view.MotionEvent
import android.view.View
import com.alteratom.R
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter

class IconIcon(
    val res: Int = 0,
    val type: String = "",
    val cat: String = ""
) : Icon() {

    override val layout = R.layout.item_icon_icon

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView

        val icon = view.findViewById<View>(R.id.iii_icon)
        icon.setBackgroundResource(res)
        icon.backgroundTintList = ColorStateList.valueOf(theme.a.pallet.a)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        val key = Icons.icons.filterValues { it == this }.keys
        setIconKey(key.first())
        (adapter as IconAdapter).onIconChange(res)
    }
}