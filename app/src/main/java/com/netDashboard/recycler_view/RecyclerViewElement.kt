package com.netDashboard.recycler_view

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.getScreenWidth
import java.util.*

abstract class RecyclerViewElement {

    var width = 1
    var height = 1

    @Transient
    var id = Random().nextLong()
    abstract val layout: Int

    @Transient
    var holder: RecyclerViewAdapter.ViewHolder? = null

    @Transient
    var adapter: RecyclerViewAdapter? = null

    @Transient
    var flag = ""
        private set

    @Transient
    var isEdit = false
        set(value) {
            field = value; onEdit(value)
        }

    fun getItemViewType(adapter: RecyclerViewAdapter): Int {
        this.adapter = adapter

        return layout
    }

    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return RecyclerViewAdapter.ViewHolder(view)
    }

    open fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        this.holder = holder

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((getScreenWidth() - view.paddingLeft * 2) / (adapter?.spanCount ?: 1)) * height
        view.layoutParams = params

        onEdit(isEdit)
        flag(flag)
    }

    fun areItemsTheSame(oldItem: RecyclerViewElement, newItem: RecyclerViewElement): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: RecyclerViewElement, newItem: RecyclerViewElement): Boolean {
        return oldItem.id == newItem.id
    }

    fun toggleFlag(flag: String) {
        if (this.flag.isNotEmpty()) {
            flag("")
        } else {
            flag(flag)
        }
    }

    fun flag(flag: String = "") {
        this.flag = flag

        val flagMark = holder?.itemView?.findViewById<View>(R.id.flag_mark)
        val flagBackground = holder?.itemView?.findViewById<View>(R.id.flag_background)

        when (flag) {
            "swap" -> flagMark?.setBackgroundResource(R.drawable.icon_swap_flag)
            "remove" -> flagMark?.setBackgroundResource(R.drawable.icon_remove_flag)
            "lock" -> flagMark?.setBackgroundResource(R.drawable.icon_lock_flag)
        }

        if (flag.isNotEmpty()) {
            flagMark?.backgroundTintList = ColorStateList.valueOf(-16777216)
            flagBackground?.setBackgroundColor((-1).alpha(.7f))

            flagMark?.visibility = View.VISIBLE
            flagBackground?.visibility = View.VISIBLE
        } else {
            flagMark?.visibility = View.GONE
            flagBackground?.visibility = View.GONE
        }
    }

    open fun onClick() {}

    open fun onEdit(isEdit: Boolean) {}
}

//flags
//to remove
//to swap
//to edit