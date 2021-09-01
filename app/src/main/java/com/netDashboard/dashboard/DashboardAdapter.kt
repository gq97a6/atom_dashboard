package com.netDashboard.dashboard

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.RecyclerViewAdapter

class DashboardAdapter(context: Context, spanCount: Int = 1) :
    RecyclerViewAdapter<Dashboard>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Dashboard>() {
        override fun areItemsTheSame(
            oldItem: Dashboard,
            newItem: Dashboard
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: Dashboard,
            newItem: Dashboard
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }
}
