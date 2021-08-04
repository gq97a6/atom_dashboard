package com.netDashboard.recycler_view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netDashboard.R
import com.netDashboard.alpha
import java.util.*

abstract class RecyclerViewElement {

    var width = 1
    var height = 1

    @Transient
    var id = kotlin.math.abs(Random().nextLong())
    abstract val layout: Int

    @Transient
    var holder: RecyclerViewAdapter.ViewHolder? = null

    @Transient
    var adapter: RecyclerViewAdapter<*>? = null

    @Transient
    var flag = Flags()

    fun <a : RecyclerViewElement> getItemViewType(adapter: RecyclerViewAdapter<a>): Int {
        this.adapter = adapter

        return layout
    }

    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return RecyclerViewAdapter.ViewHolder(view)
    }

    open fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        this.holder = holder
        onEdit(!(adapter?.editType?.isNone ?: true))
    }

    fun areItemsTheSame(oldItem: RecyclerViewElement, newItem: RecyclerViewElement): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: RecyclerViewElement, newItem: RecyclerViewElement): Boolean {
        return oldItem.id == newItem.id
    }

    open fun onClick() {}

    open fun onEdit(isEdit: Boolean) {}

    inner class Flags {
        private var flag = -1

        private val isNone
            get() = flag == -1
        val isSwap
            get() = flag == 0
        val isRemove
            get() = flag == 1
        val isLock
            get() = flag == 2

        fun setNone() = setFlag(-1)
        fun setSwap() = setFlag(0)
        fun setRemove() = setFlag(1)
        fun setLock() = setFlag(2)

        private fun setFlag(type: Int) {
            flag = type
            show()
        }

        private fun show() {
            val flagMark = holder?.itemView?.findViewById<View>(R.id.flag_mark)
            val flagBackground = holder?.itemView?.findViewById<View>(R.id.flag_background)

            if (!isNone) {
                flagMark?.setBackgroundResource(
                    when {
                        isSwap -> R.drawable.icon_swap_flag
                        isRemove -> R.drawable.icon_remove_flag
                        isLock -> R.drawable.icon_lock_flag
                        else -> R.drawable.icon_lock_flag
                    }
                )

                flagMark?.backgroundTintList = ColorStateList.valueOf(-16777216)
                flagBackground?.setBackgroundColor((-1).alpha(.7f))

                flagMark?.visibility = View.VISIBLE
                flagBackground?.visibility = View.VISIBLE
            } else {
                flagMark?.visibility = View.GONE
                flagBackground?.visibility = View.GONE
            }
        }
    }
}