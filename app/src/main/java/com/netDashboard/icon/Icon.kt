package com.netDashboard.icon

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth

class Icon(
    val res: Int = 0,
    val cat: String,
    val type: String = "",
    val isCategory: Boolean = false
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

        if (isCategory) {
            text.text = cat
            text.visibility = VISIBLE
            icon.visibility = GONE
        } else {
            icon.setBackgroundResource(res)
            icon.visibility = VISIBLE
            text.visibility = GONE
        }
    }
}