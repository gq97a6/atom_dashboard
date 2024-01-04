package com.alteratom.dashboard.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alteratom.R
import com.alteratom.dashboard.click
import com.alteratom.dashboard.iterate
import com.alteratom.dashboard.`object`.DialogBuilder.buildConfirm

@Suppress("UNUSED")
open class RecyclerViewAdapter<item : RecyclerViewItem>(
    var context: Context,
    var spanCount: Int = 1,
) : ListAdapter<item, RecyclerViewAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<item>() {
        override fun areItemsTheSame(oldItem: item, newItem: item): Boolean =
            areItemsTheSame(oldItem, newItem)

        override fun areContentsTheSame(oldItem: item, newItem: item): Boolean =
            areContentsTheSame(oldItem, newItem)
    }
) {

    var editMode = Modes()

    var list: MutableList<item> = mutableListOf()
    private lateinit var currentItem: item
    private lateinit var touchHelper: ItemTouchHelper

    var onBindViewHolder: (item, ViewHolder, Int) -> Unit = { _, _, _ -> }
    var onItemClick: (item) -> Unit = {}
    var onItemLongClick: (item) -> Unit = {}
    var onItemRemoved: (item) -> Unit = {}
    var onItemMarkedRemove: (Int, Boolean) -> Unit = { _, _ -> }
    var onItemEdit: (item) -> Unit = {}

    override fun submitList(list: MutableList<item>?) {
        super.submitList(list)
        this.list = list ?: mutableListOf()
    }

    override fun getCurrentList(): MutableList<item> {
        return list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) { //0 (order of execution)
        super.onAttachedToRecyclerView(recyclerView)

        touchHelper = ItemTouchHelper(ItemTouchCallback(this))
        touchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int { //1 (order of execution)
        currentItem = list[position]
        return list[position].getItemViewType(this)
    }

    override fun onCreateViewHolder( //2 (order of execution)
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return currentItem.onCreateViewHolder(parent, viewType)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) { //4 (order of execution)
        super.onViewAttachedToWindow(holder)
        currentItem.onViewAttachedToWindow(holder)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) { //3 (order of execution)
        currentItem.onBindViewHolder(holder, position)
        currentItem.onSetTheme(holder)

        val callback = { v: View ->
            var isLongPressed = false

            v.isClickable = true
            v.setOnTouchListener { v, e ->
                if (editMode.isNone) list[position].onTouch(v, e)

                if (e.action == ACTION_DOWN) {
                    isLongPressed = false
                    if (editMode.isSwap) {
                        list[position].holder?.let {
                            touchHelper.startDrag(it)
                        }
                    } else {
                        val ripple = holder.itemView.findViewById<View>(R.id.ripple_foreground)
                        ripple?.click()
                    }
                }

                if (e.action == ACTION_UP && !isLongPressed) { // onClick
                    onItemClick(list[position])

                    when {
                        editMode.isNone -> list[position].onClick(v, e)
                        editMode.isRemove -> {
                            markItemRemove(position)
                            //removeMarkedItems()
                        }

                        editMode.isEdit -> onItemEdit(list[position])
                    }
                }

                if (e.eventTime - e.downTime > 300 && !isLongPressed) { // onLongClick
                    isLongPressed = true
                    onItemLongClick(list[position])
                }

                return@setOnTouchListener false
            }
        }

        holder.itemView as ViewGroup
        (holder.itemView as ViewGroup).iterate(callback)
        onBindViewHolder(list[position], holder, position)
    }

    private fun markItemRemove(position: Int) {
        val recyclerView =
            list[position].holder?.itemView?.parent as RecyclerView

        recyclerView.itemAnimator?.changeDuration = 250

        list[position].flag.let {
            if (it.isRemove) it.setNone() else it.setRemove()
            onItemMarkedRemove(list.count { item: item -> item.flag.isRemove }, it.isRemove)
        }
    }

    fun removeMarkedItems() {
        if (list.none { item: item -> item.flag.isRemove } || itemCount == 0) return

        with(context) {
            buildConfirm("Confirm removing", "CONFIRM") {
                var i = 0
                while (i < list.size) {
                    list[i].let {
                        if (it.flag.isRemove) {
                            removeItemAt(i, false)
                            onItemRemoved(it)
                            i--
                        }
                    }
                    i++
                }
            }
        }
    }

    fun removeItemAt(pos: Int, notify: Boolean = true) {
        val callback = { v: View ->
            v.setOnTouchListener(null)
            v.setOnClickListener(null)
        }

        (list[pos].holder?.itemView as? ViewGroup)?.let {
            it.iterate(callback)
            list.removeAt(pos)

            if (notify) notifyItemRemoved(pos)
        }
    }

    open inner class Modes {
        private var mode = -1

        var onSet: (Modes) -> Unit = {}

        val isNone
            get() = mode == -1
        val isSwap
            get() = mode == 0
        val isRemove
            get() = mode == 1
        val isEdit
            get() = mode == 2

        fun setNone() = setMode(-1)
        fun setSwap() = setMode(0)
        fun setRemove() = setMode(1)
        fun setEdit() = setMode(2)

        private fun setMode(type: Int) {
            mode = type
            onSet(this)
            for (e in list) {
                e.flag.setNone()
                e.onEdit(!isNone)
            }
        }
    }
}