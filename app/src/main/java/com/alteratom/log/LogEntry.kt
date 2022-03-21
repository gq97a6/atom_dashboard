package com.alteratom.log

import android.annotation.SuppressLint
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.alteratom.R
import com.alteratom.recycler_view.RecyclerViewAdapter
import com.alteratom.recycler_view.RecyclerViewItem
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class LogEntry(
    var text: String = "Example log. Test Log."
) : RecyclerViewItem() {

    @JsonIgnore
    override val layout = R.layout.item_log

    private val time = SimpleDateFormat("hh:mm:ss").format(Date())

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        " $time ".let {
            holder.itemView.findViewById<TextView>(R.id.il_time).text = it
        }
        holder.itemView.findViewById<TextView>(R.id.il_text).text = text
    }
}