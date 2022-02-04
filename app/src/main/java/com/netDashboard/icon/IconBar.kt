package com.netDashboard.icon

import android.widget.LinearLayout
import com.google.android.material.chip.ChipGroup
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconBar : Icon() {

    override val layout = R.layout.item_icon_bar
    override val spanCount = -1

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        view.layoutParams = params

        holder.itemView.findViewById<ChipGroup>(R.id.iib_icon_type)
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.iib_line -> (adapter as IconAdapter).applyIconSet("l")
                    R.id.iib_thin -> (adapter as IconAdapter).applyIconSet("t")
                    R.id.iib_solid -> (adapter as IconAdapter).applyIconSet("s")
                }
            }
    }
}