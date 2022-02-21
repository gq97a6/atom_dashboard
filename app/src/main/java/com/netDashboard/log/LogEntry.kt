package com.netDashboard.log

import android.annotation.SuppressLint
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class LogEntry(
    var text: String = "Example log. Test Log."
) : RecyclerViewItem() {

    @JsonIgnore
    override val layout = R.layout.item_log

    private val time = SimpleDateFormat("hh:mm:ss").format(Date())
    private val date = SimpleDateFormat("dd.M.yy").format(Date())

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<TextView>(R.id.il_date).text = date
        " $time: ".let {
            holder.itemView.findViewById<TextView>(R.id.il_time).text = it
        }
        holder.itemView.findViewById<TextView>(R.id.il_text).text = text
    }
}