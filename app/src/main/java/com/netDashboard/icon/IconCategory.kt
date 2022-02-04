package com.netDashboard.icon

import android.widget.LinearLayout
import android.widget.TextView
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconCategory(
    val cat: String = "Test",
) : Icon() {

    override val layout = R.layout.item_icon_category
    override val spanCount = -1

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        view.layoutParams = params

        holder.itemView.findViewById<TextView>(R.id.iic_text).text = cat
    }
}