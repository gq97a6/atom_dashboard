package com.netDashboard.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.netDashboard.R
import com.netDashboard.click
import com.netDashboard.globals.G
import java.util.*

@Suppress("UNUSED")
abstract class BaseRecyclerViewAdapter<item : BaseRecyclerViewItem>(
    var context: Context,
    var spanCount: Int,
    c: DiffUtil.ItemCallback<item>
) : ListAdapter<item, BaseRecyclerViewAdapter.ViewHolder>(c) {

    var editType = Modes()

    var theme = G.theme
    lateinit var list: MutableList<item>
    private lateinit var currentItem: item
    private lateinit var touchHelper: ItemTouchHelper

    var onItemClick: (item) -> Unit = {}
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

    override fun getItemViewType(position: Int): Int {
        currentItem = list[position]
        return list[position].getItemViewType(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return currentItem.onCreateViewHolder(parent, viewType)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        touchHelper = ItemTouchHelper(ItemTouchCallback(this))
        touchHelper.attachToRecyclerView(recyclerView)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].onBindViewHolder(holder, position)

        if (editType.isNone) theme.apply(context, holder.itemView as ViewGroup)

        fun View.setOnClick() {
            this.setOnTouchListener { v, e ->
                if (editType.isNone) list[position].onTouch(v, e)

                if (e.action == ACTION_DOWN) {
                    if (editType.isSwap) {
                        list[position].holder?.let {
                            touchHelper.startDrag(it)
                        }
                    } else {
                        val foreground = holder.itemView.findViewById<View>(R.id.foreground)
                        foreground?.click()
                    }
                }

                if (e.action == ACTION_UP) {
                    this.performClick()

                    onItemClick(list[position])

                    when {
                        editType.isNone -> list[position].onClick(v, e)
                        editType.isRemove -> {
                            markItemRemove(position)
                            //removeMarkedItems()
                        }
                        editType.isEdit -> onItemEdit(list[position])
                    }
                }

                return@setOnTouchListener true
            }
        }

        fun ViewGroup.iterate() {
            for (i in 0 until this.childCount) {
                val v = this.getChildAt(i)

                if (v is ViewGroup) v.iterate()
                v.setOnClick()
            }

            this.setOnClick()
        }

        (holder.itemView as ViewGroup).iterate()
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

    @SuppressLint("NotifyDataSetChanged")
    fun removeMarkedItems() {

        if (list.none { item: item -> item.flag.isRemove } || itemCount == 0) return

        //@SuppressLint("ShowToast")
        //val snackbar = list[0].holder?.itemView?.rootView?.let {
        //    Snackbar.make(
        //        it,
        //        context.getString(R.string.snackbar_confirmation),
        //        Snackbar.LENGTH_LONG
        //    ).setAction("YES") {
        //        for (i in list) {
        //            if (i.flag.isRemove) onItemRemove(i)
        //        }
        //        list.removeAll { item: item -> item.flag.isRemove }
        //        notifyDataSetChanged()
        //    }
        //}
//
        //val snackBarView = snackbar?.view
        //snackBarView?.translationY = -90.toPx().toFloat()
        //snackbar?.show()

        for (i in list) {
            if (i.flag.isRemove) onItemRemoved(i)
        }
        list.removeAll { item: item -> item.flag.isRemove }
        notifyDataSetChanged()
    }

    open inner class Modes {
        private var mode = -1

        var onSetMode: (Modes) -> Unit = {}

        val isNone
            get() = mode == -1
        val isSwap
            get() = mode == 0
        val isRemove
            get() = mode == 1
        val isEdit
            get() = mode == 2
        val isAdd
            get() = mode == 3
        val isLock
            get() = mode == 4

        fun setNone() = setMode(-1)
        fun setSwap() = setMode(0)
        fun setRemove() = setMode(1)
        fun setEdit() = setMode(2)
        fun setAdd() = setMode(3)
        fun setLock() = setMode(4)

        private fun setMode(type: Int) {
            mode = type
            onSetMode(this)
            for (e in list) {
                e.flag.setNone()
                e.onEdit(!isNone)
            }
        }
    }
}