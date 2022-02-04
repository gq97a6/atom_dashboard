package com.netDashboard.icon

import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.toPx

class IconGap : Icon() {

    override val layout = R.layout.item_icon_gap
    override val spanCount = -1

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = 140f.toPx()
        view.layoutParams = params
    }
}