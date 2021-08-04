package com.netDashboard.tile

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.netDashboard.recycler_view.RecyclerViewAdapter

class TilesAdapter(context: Context, spanCount: Int) : RecyclerViewAdapter<Tile>(context, spanCount, DiffCallback) {

    object DiffCallback : DiffUtil.ItemCallback<Tile>() {
        override fun areItemsTheSame(
            oldItem: Tile,
            newItem: Tile
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: Tile,
            newItem: Tile
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }
}

