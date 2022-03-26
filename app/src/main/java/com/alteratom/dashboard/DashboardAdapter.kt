package com.alteratom.dashboard

import android.content.Context
import androidx.recyclerview.widget.DiffUtil

class DashboardAdapter(context: Context, spanCount: Int = 1) :
    com.alteratom.dashboard.recycler_view.RecyclerViewAdapter<Dashboard>(context, spanCount, DiffCallback) {

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
