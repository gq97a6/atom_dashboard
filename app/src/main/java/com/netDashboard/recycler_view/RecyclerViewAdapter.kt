package com.netDashboard.recycler_view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

abstract class RecyclerViewAdapter :
    ListAdapter<RecyclerViewElement, RecyclerViewAdapter.ViewHolder>(DiffCallback) {

    private var mode = ""
    private var context: Context? = null
    private var swapLock = false
    var spanCount = 0

    lateinit var list: MutableList<RecyclerViewElement>
    private lateinit var current: RecyclerViewElement

    private val tileOnClick = MutableLiveData(-1)

    var isEdit
        get() = swapMode || removeMode || addMode || editMode
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            mode = if (value) {
                swapLock = false
                "edit"
            } else {
                ""
            }

            for (t in list) {
                t.isEdit = value
                t.flag()
            }

            notifyDataSetChanged()
        }

    private fun mode(type: String): Boolean {
        return mode == type
    }

    private fun mode(type: String, b: Boolean) {
        if (b) {
            mode = type

            for (t in list) {
                t.flag()
            }
        }

    }

    private var swapMode
        get() = mode == "swap"
        set(value) {
            mode("swap", value)
        }

    private var removeMode
        get() = mode == "remove"
        set(value) {
            mode("remove", value)
        }

    private var editMode
        get() = mode == "edit"
        set(value) {
            mode("edit", value)
        }

    private var addMode
        get() = mode == "add"
        set(value) {
            mode("add", value)
        }

    fun getTileOnClickLiveData(): LiveData<Int> {
        return tileOnClick
    }

    override fun submitList(list: MutableList<RecyclerViewElement>?) {
        super.submitList(list)
        this.list = list ?: mutableListOf()
    }

    override fun getCurrentList(): MutableList<RecyclerViewElement> {
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
            tileOnClick.postValue(position)

            when {
                swapMode -> {
                    swapTiles(position)
                }
                removeMode -> {
                    removeTile(position)
                }
                editMode -> {
                    //TODO
                }
                addMode -> {
                    tileOnClick.postValue(position)
                }
                else -> {
                    list[position].onClick()
                }
            }
        }
    }

    private fun swapTiles(position: Int) {

        if (!swapLock) {

            if (list[position].flag != "lock") {
                list[position].toggleFlag("swap")
            }

            for ((pos, t) in list.withIndex()) {

                if (t.flag == "swap" && list[position].id != t.id) {
                    val recyclerView =
                        list[position].holder?.itemView?.parent as RecyclerView

                    recyclerView.itemAnimator?.changeDuration = 0

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager

                    val tileHeight = list[position].holder?.itemView?.height ?: 1
                    val max = (recyclerView.height / tileHeight) * spanCount
                    val f = layoutManager.findFirstVisibleItemPosition()
                    val l = layoutManager.findLastVisibleItemPosition()

                    list[pos].flag()
                    list[position].flag()

                    if (abs(position - pos + 1) <= max && position in f..l && pos in f..l) {
                        swapLock = true

                        recyclerView.suppressLayout(true)

                        list[pos].flag("lock")
                        list[position].flag("lock")

                        val xyA = IntArray(2)
                        list[pos].holder?.itemView?.getLocationOnScreen(xyA)

                        val xyB = IntArray(2)
                        list[position].holder?.itemView?.getLocationOnScreen(xyB)

                        list[pos].holder?.itemView?.elevation = 2f
                        list[position].holder?.itemView?.elevation = 1f

                        val xA = list[pos].holder?.itemView?.x ?: 0f
                        val xB = list[position].holder?.itemView?.x ?: 0f

                        val yA = list[pos].holder?.itemView?.y ?: 0f
                        val yB = list[position].holder?.itemView?.y ?: 0f

                        val distance = kotlin.math.sqrt(
                            (xA - xB).toDouble().pow(2) + (yA - yB).toDouble().pow(2)
                        )
                        val duration = (distance * 0.5).toLong()

                        list[pos].holder?.itemView?.animate()?.cancel()

                        list[pos].holder?.itemView?.animate()
                            ?.x(xB)
                            ?.y(yB)
                            ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                            duration

                        list[position].holder?.itemView?.animate()
                            ?.x(xA)
                            ?.y(yA)
                            ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                            duration

                        Handler(Looper.getMainLooper()).postDelayed({
                            recyclerView.suppressLayout(false)

                            list[pos].flag()
                            list[position].flag()

                            Collections.swap(list, position, pos)
                            notifyItemChanged(position)
                            notifyItemChanged(pos)

                            list[pos].holder?.itemView?.elevation = 0f
                            list[position].holder?.itemView?.elevation = 0f

                            swapLock = false
                        }, duration + 50)
                    } else {
                        Collections.swap(list, position, pos)
                        notifyItemChanged(position)
                        notifyItemChanged(pos)
                    }
                }
            }
        }
    }

    private fun removeTile(position: Int) {
        val recyclerView =
            list[position].holder?.itemView?.parent as RecyclerView

        recyclerView.itemAnimator?.changeDuration = 250

        for ((i, t) in list.withIndex()) {

            if (t.flag == "remove" && list[position].id != t.id) {
                list[i].flag()
            }
        }

        list[position].toggleFlag("remove")
    }
}

object DiffCallback : DiffUtil.ItemCallback<RecyclerViewElement>() {
    override fun areItemsTheSame(
        oldItem: RecyclerViewElement,
        newItem: RecyclerViewElement
    ): Boolean {
        return oldItem.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(
        oldItem: RecyclerViewElement,
        newItem: RecyclerViewElement
    ): Boolean {
        return oldItem.areContentsTheSame(oldItem, newItem)
    }
}