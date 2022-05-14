package com.alteratom.dashboard.icon

import android.widget.LinearLayout
import com.alteratom.R
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.google.android.material.chip.ChipGroup

class IconBar : Icon() {

    override val layout = R.layout.item_icon_bar
    override val spanCount = -1

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height = LinearLayout.LayoutParams.WRAP_CONTENT
        view.layoutParams = params

        holder.itemView.findViewById<ChipGroup>(R.id.iib_icon_type)
            .setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.iib_line -> (adapter as IconAdapter).applyIconSet("l")
                    R.id.iib_solid -> (adapter as IconAdapter).applyIconSet("s")
                    R.id.iib_thin -> (adapter as IconAdapter).applyIconSet("t")
                }
            }
    }
}