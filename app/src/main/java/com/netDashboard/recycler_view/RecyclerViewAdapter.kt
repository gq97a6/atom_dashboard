package com.netDashboard.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.toPx
import java.util.*

abstract class RecyclerViewAdapter<element : RecyclerViewElement>(
    var context: Context,
    var spanCount: Int,
    c: DiffUtil.ItemCallback<element>
) :
    ListAdapter<element, RecyclerViewAdapter.ViewHolder>(c) {
    var editType = Modes()

    lateinit var list: MutableList<element>
    private lateinit var current: element

    private val onClick = MutableLiveData(-1)
    val onRemove = MutableLiveData(-1)

    fun getOnClick(): LiveData<Int> {
        return onClick
    }

    override fun submitList(list: MutableList<element>?) {
        super.submitList(list)
        this.list = list ?: mutableListOf()
    }

    override fun getCurrentList(): MutableList<element> {
        return list
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    override fun getItemViewType(position: Int): Int {
        current = list[position]
        return list[position].getItemViewType(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return current.onCreateViewHolder(parent, viewType)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        list[position].onBindViewHolder(holder, position)
        holder.itemView.setOnClickListener {
            onClick.postValue(position)

            when {
                editType.isNone -> {
                    list[position].onClick()
                }
                editType.isSwap -> {
                    if (!editType.isLock) {
                        markElementSwap(position)
                        swapMarkedElements(position)
                    }
                }
                editType.isRemove -> {
                    markElementRemove(position)
                }
            }
        }
    }

    private fun markElementRemove(position: Int) {
        val recyclerView =
            list[position].holder?.itemView?.parent as RecyclerView

        recyclerView.itemAnimator?.changeDuration = 250

        for ((i, t) in list.withIndex()) {

            if (t.flag.isRemove && list[position].id != t.id) {
                list[i].flag.setNone()
            }
        }

        list[position].flag.setRemove()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeMarkedElement() {

        var removeAt = -1
        for (e in list) {
            if (e.flag.isRemove) {
                removeAt = e.holder?.adapterPosition ?: -1
                break
            }
        }

        if (removeAt == -1 || itemCount == 0) return

        @SuppressLint("ShowToast")
        val snackbar = list[0].holder?.itemView?.rootView?.let {
            Snackbar.make(
                it,
                context.getString(R.string.snackbar_confirmation),
                Snackbar.LENGTH_LONG
            ).setAction("YES") {
                if (list[removeAt].flag.isRemove) {
                    list.removeAt(removeAt)
                    notifyDataSetChanged()
                    onRemove.postValue(removeAt)
                }
            }
        }

        val snackBarView = snackbar?.view
        snackBarView?.translationY = -60.toPx().toFloat()
        snackbar?.show()
    }

    private fun markElementSwap(position: Int) {
        if (!list[position].flag.isLock) {
            list[position].flag.let {
                if (!it.isSwap) it.setSwap() else it.setNone()
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun swapMarkedElements(position: Int) {

        for ((pos, t) in list.withIndex()) {

            if (t.flag.isSwap && list[position].id != t.id) {

                list[pos].flag.setNone()
                list[position].flag.setNone()

                Collections.swap(list, position, pos)
                notifyDataSetChanged()
            }
        }
    }

    inner class Modes {
        private var mode = -1

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

            for (e in list) {
                e.flag.setNone()
                e.onEdit(!isNone)
            }
        }
    }
}