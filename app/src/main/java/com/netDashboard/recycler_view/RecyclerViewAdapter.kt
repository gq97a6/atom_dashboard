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
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.toPx
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

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
                val recyclerView =
                    list[position].holder?.itemView?.parent as RecyclerView

                recyclerView.itemAnimator?.changeDuration = 0

                val layoutManager = recyclerView.layoutManager as GridLayoutManager

                val elementHeight = list[position].holder?.itemView?.height ?: 1
                val max = (recyclerView.height / elementHeight) * spanCount
                val f = layoutManager.findFirstVisibleItemPosition()
                val l = layoutManager.findLastVisibleItemPosition()

                list[pos].flag.setNone()
                list[position].flag.setNone()

                if (abs(position - pos + 1) <= max && position in f..l && pos in f..l) {
                    editType.setLock()

                    recyclerView.suppressLayout(true)

                    list[pos].flag.setLock()
                    list[position].flag.setLock()

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

                        list[pos].flag.setNone()
                        list[position].flag.setNone()

                        Collections.swap(list, position, pos)
                        notifyItemChanged(position)
                        notifyItemChanged(pos)

                        list[pos].holder?.itemView?.elevation = 0f
                        list[position].holder?.itemView?.elevation = 0f

                        if (editType.isLock) editType.setSwap()

                        Handler(Looper.getMainLooper()).postDelayed({
                            notifyDataSetChanged()
                        }, 0)
                    }, duration + 50)
                } else {
                    Collections.swap(list, position, pos)
                    notifyItemChanged(position)
                    notifyItemChanged(pos)
                }
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

        @SuppressLint("NotifyDataSetChanged")
        private fun setMode(type: Int) {
            mode = type
            for (e in list) {
                e.flag.setNone()
                e.onEdit(!isNone)
            }
        }
    }
}