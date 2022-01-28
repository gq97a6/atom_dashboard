package com.netDashboard.icon

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class IconPropertiesDrawableAdapter(context: Context, spanCount: Int) :
    BaseRecyclerViewAdapter<IconPropertiesDrawable>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<IconPropertiesDrawable>() {
        override fun areItemsTheSame(
            oldItem: IconPropertiesDrawable,
            newItem: IconPropertiesDrawable
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: IconPropertiesDrawable,
            newItem: IconPropertiesDrawable
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

