package com.alteratom.icon

import com.alteratom.recycler_view.RecyclerViewAdapter
import com.alteratom.recycler_view.RecyclerViewItem
import com.alteratom.screenWidth

abstract class Icon : RecyclerViewItem() {
    open val spanCount = 1

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = (screenWidth - view.paddingLeft * 2) / adapter.spanCount
        view.layoutParams = params
    }
}