package com.netDashboard.recycler_view

import android.content.Context
import androidx.recyclerview.widget.DiffUtil

class RecyclerViewAdapter(context: Context, spanCount: Int = 1) :
    BaseRecyclerViewAdapter<RecyclerViewItem>(context, spanCount, DiffCallback) {

    var onBindViewHolder: (RecyclerViewItem, ViewHolder, Int) -> Unit = { _, _, _ -> }

    object DiffCallback : DiffUtil.ItemCallback<RecyclerViewItem>() {
        override fun areItemsTheSame(
            oldItem: RecyclerViewItem,
            newItem: RecyclerViewItem
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: RecyclerViewItem,
            newItem: RecyclerViewItem
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        onBindViewHolder(list[position], holder, position)
    }
}