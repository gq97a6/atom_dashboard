package com.netDashboard.recycler_view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.id_generator.IdGenerator

@Suppress("UNUSED")
abstract class BaseRecyclerViewItem {

    var width = 1
    var height = 1

    var id = IdGenerator.getId()

    abstract val layout: Int

    @Transient
    var holder: BaseRecyclerViewAdapter.ViewHolder? = null

    @Transient
    var adapter: BaseRecyclerViewAdapter<*>? = null
    //lateinit var adapter: BaseRecyclerViewAdapter<*>

    @Transient
    var flag = Flags()

    fun <a : BaseRecyclerViewItem> getItemViewType(adapter: BaseRecyclerViewAdapter<a>): Int {
        this.adapter = adapter

        return layout
    }

    open fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)

        return BaseRecyclerViewAdapter.ViewHolder(view)
    }

    open fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        this.holder = holder
        onEdit(!(adapter?.editType?.isNone ?: true))
    }

    fun areItemsTheSame(oldItem: BaseRecyclerViewItem, newItem: BaseRecyclerViewItem): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: BaseRecyclerViewItem, newItem: BaseRecyclerViewItem): Boolean {
        return oldItem.id == newItem.id
    }

    open fun onTouch(v: View, e: MotionEvent) {}

    open fun onClick(v: View, e: MotionEvent) {}

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

                flagMark?.backgroundTintList = ColorStateList.valueOf(adapter?.theme!!.color)
                flagBackground?.setBackgroundColor(adapter?.theme!!.colorD.alpha(190))

                flagMark?.animate()
                    ?.alpha(1f)
                    ?.withStartAction { flagMark.visibility = View.VISIBLE }
                    ?.duration = 150

                flagBackground?.animate()
                    ?.alpha(1f)
                    ?.withStartAction { flagBackground.visibility = View.VISIBLE }
                    ?.duration = 150
            } else {
                flagMark?.animate()
                    ?.alpha(0f)
                    ?.withEndAction { flagMark.visibility = View.GONE }
                    ?.duration = 150

                flagBackground?.animate()
                    ?.alpha(0f)
                    ?.withEndAction { flagBackground.visibility = View.GONE }
                    ?.duration = 150
            }
        }
    }
}