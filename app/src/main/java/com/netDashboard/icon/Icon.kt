package com.netDashboard.icon

import android.view.View
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth

class Icon(val res: Int, val cat: String, val type: String) : BaseRecyclerViewItem() {
    override val layout = R.layout.item_icon

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = (screenWidth - view.paddingLeft * 2) / adapter.spanCount
        view.layoutParams = params
        view.findViewById<View>(R.id.ic_icon).setBackgroundResource(res)
    }
}