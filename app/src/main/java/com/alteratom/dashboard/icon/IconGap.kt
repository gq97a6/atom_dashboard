package com.alteratom.dashboard.icon

import com.alteratom.R
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.toPx

class IconGap : Icon() {

    override val layout = R.layout.item_icon_gap
    override val spanCount = -1

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = 140f.toPx()
        view.layoutParams = params
    }
}