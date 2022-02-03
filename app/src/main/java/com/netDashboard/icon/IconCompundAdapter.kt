package com.netDashboard.icon

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconCompundAdapter(context: Context, spanCount: Int) :
    BaseRecyclerViewAdapter<IconCompound>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<IconCompound>() {
        override fun areItemsTheSame(
            oldItem: IconCompound,
            newItem: IconCompound
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: IconCompound,
            newItem: IconCompound
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

