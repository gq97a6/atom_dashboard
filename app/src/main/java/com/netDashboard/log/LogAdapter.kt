package com.netDashboard.log

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class LogAdapter(context: Context, spanCount: Int = 1) :
    BaseRecyclerViewAdapter<LogEntry>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<LogEntry>() {
        override fun areItemsTheSame(
            oldItem: LogEntry,
            newItem: LogEntry
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: LogEntry,
            newItem: LogEntry
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
    }
}
