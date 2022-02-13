package com.netDashboard.recycler_view

import android.content.Context
import androidx.recyclerview.widget.DiffUtil

class GenericAdapter(context: Context, spanCount: Int = 1) :
    RecyclerViewAdapter<GenericItem>(context, spanCount, DiffCallback) {

    var onBindViewHolder: (GenericItem, ViewHolder, Int) -> Unit = { _, _, _ -> }

    object DiffCallback : DiffUtil.ItemCallback<GenericItem>() {
        override fun areItemsTheSame(
            oldItem: GenericItem,
            newItem: GenericItem
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: GenericItem,
            newItem: GenericItem
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        onBindViewHolder(list[position], holder, position)
    }
}