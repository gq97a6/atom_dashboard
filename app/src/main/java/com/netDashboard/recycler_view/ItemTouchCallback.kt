package com.netDashboard.recycler_view

import android.annotation.SuppressLint
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.ItemTouchHelper.*
import java.util.*

class ItemTouchCallback(private val adapter: BaseRecyclerViewAdapter<*>) :
    ItemTouchHelper.Callback() {

    var onMove = {}

    var onSelectedChanged = {}

    var onClear = {}

    var onSwiped = {}

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = recyclerView.layoutManager.let {
            when (it) {
                is GridLayoutManager -> UP or DOWN or LEFT or RIGHT
                is StaggeredGridLayoutManager -> UP or DOWN or LEFT or RIGHT
                is LinearLayoutManager -> UP or DOWN
                else -> UP or DOWN
            }
        }

        return makeMovementFlags(dragFlags, 0)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        val from = viewHolder.adapterPosition
        val to = target.adapterPosition

        if (from < to) {
            for (i in from until to) {
                Collections.swap(adapter.list, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(adapter.list, i, i - 1)
            }
        }

        adapter.notifyDataSetChanged()

        onMove()

        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState != ACTION_STATE_IDLE && viewHolder is BaseRecyclerViewAdapter.ViewHolder) {
            onSelectedChanged()
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)

        onClear()
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        onSwiped()
    }

    override fun isLongPressDragEnabled(): Boolean = false
}
