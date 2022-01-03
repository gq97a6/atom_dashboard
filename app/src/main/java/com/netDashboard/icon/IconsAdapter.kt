package com.netDashboard.icon

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconsAdapter(context: Context, spanCount: Int) :
    BaseRecyclerViewAdapter<Icon>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Icon>() {
        override fun areItemsTheSame(
            oldItem: Icon,
            newItem: Icon
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: Icon,
            newItem: Icon
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

