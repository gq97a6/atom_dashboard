package com.netDashboard.log

import android.widget.TextView
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem

class LogEntry(
    private var time: String = "07:47",
    private var date: String = "23.09.21",
    var text: String = "Example log. Test Log."
) : BaseRecyclerViewItem() {
    override val layout = R.layout.item_log

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<TextView>(R.id.il_date).text = date
        " $time: ".let {
            holder.itemView.findViewById<TextView>(R.id.il_time).text = it
        }
        holder.itemView.findViewById<TextView>(R.id.il_text).text = text
    }
}