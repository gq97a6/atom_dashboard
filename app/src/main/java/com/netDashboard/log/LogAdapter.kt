package com.netDashboard.log

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.RecyclerViewAdapter

class LogAdapter(context: Context, spanCount: Int = 1) :
    RecyclerViewAdapter<LogEntry>(context, spanCount, DiffCallback) {

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

}
