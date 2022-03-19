package com.netDashboard.recycler_view

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.IdGenerator
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.G.theme

@Suppress("UNUSED")
abstract class RecyclerViewItem {

    var id = IdGenerator.getId()

    abstract val layout: Int

    @JsonIgnore
    var holder: RecyclerViewAdapter.ViewHolder? = null

    @JsonIgnore
    lateinit var adapter: RecyclerViewAdapter<*>

    @JsonIgnore
    var flag = Flags()

    init {
        IdGenerator.reportTakenId(id)
    }

    fun <a : RecyclerViewItem> getItemViewType(adapter: RecyclerViewAdapter<a>): Int {
        this.adapter = adapter

        return layout
    }

    open fun onCreateViewHolder( //2 (order of execution)
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return RecyclerViewAdapter.ViewHolder(view)
    }

    open fun onViewAttachedToWindow(holder: RecyclerViewAdapter.ViewHolder) { //4 (order of execution)
    }

    open fun onBindViewHolder(
        holder: RecyclerViewAdapter.ViewHolder,
        position: Int
    ) { //3 (order of execution)
        this.holder = holder
        onEdit(!(adapter.editMode.isNone))
    }

    fun areItemsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: RecyclerViewItem, newItem: RecyclerViewItem): Boolean {
        return oldItem.id == newItem.id
    }

    open fun onTouch(v: View, e: MotionEvent) {}

    open fun onClick(v: View, e: MotionEvent) {}

    open fun onEdit(isEdit: Boolean) {}

    inner class Flags {
        private var flag = -1

        private val isNone
            get() = flag == -1
        val isRemove
            get() = flag == 1

        fun setNone() = setFlag(-1)
        fun setRemove() = setFlag(1)

        private fun setFlag(type: Int) {
            flag = type
            show()
        }

        private fun show() {
            val foreground = holder?.itemView?.findViewById<View>(R.id.foreground)

            if (!isNone) {
                foreground?.setBackgroundColor(theme.a.colorPallet.background.alpha(190))

                foreground?.animate()
                    ?.alpha(1f)
                    ?.withStartAction { foreground.visibility = View.VISIBLE }
                    ?.duration = 150
            } else {
                foreground?.animate()
                    ?.alpha(0f)
                    ?.withEndAction { foreground.visibility = View.GONE }
                    ?.duration = 150
            }
        }
    }
}