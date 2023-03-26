package com.alteratom.dashboard.log

import android.annotation.SuppressLint
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.fasterxml.jackson.annotation.JsonIgnore
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
class LogEntry(
    var text: String = "Example log. Test Log."
) : RecyclerViewItem() {

    @JsonIgnore
    override val layout = R.layout.item_log

    private val time =
        SimpleDateFormat(if (settings.militaryTime) "HH:mm:ss" else "hh:mm:ss").format(Date())

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        " $time ".let {
            holder.itemView.findViewById<TextView>(R.id.il_time).text = it
        }
        holder.itemView.findViewById<TextView>(R.id.il_text).text = text
    }
}