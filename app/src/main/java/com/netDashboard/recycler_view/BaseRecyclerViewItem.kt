package com.netDashboard.recycler_view

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.IdGenerator
import com.netDashboard.R
import com.netDashboard.Theme
import com.netDashboard.alpha
import com.netDashboard.globals.G.theme

@Suppress("UNUSED")
abstract class BaseRecyclerViewItem {

    var id = IdGenerator.getId()

    abstract val layout: Int

    @JsonIgnore
    var holder: BaseRecyclerViewAdapter.ViewHolder? = null

    @JsonIgnore
    lateinit var adapter: BaseRecyclerViewAdapter<*>

    @JsonIgnore
    var flag = Flags()

    init {
        IdGenerator.reportTakenId(id)
    }

    fun <a : BaseRecyclerViewItem> getItemViewType(adapter: BaseRecyclerViewAdapter<a>): Int {
        this.adapter = adapter

        return layout
    }

    open fun onCreateViewHolder( //2 (order of execution)
        parent: ViewGroup,
        viewType: Int
    ): BaseRecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return BaseRecyclerViewAdapter.ViewHolder(view)
    }

    open fun onViewAttachedToWindow(holder: BaseRecyclerViewAdapter.ViewHolder) { //4 (order of execution)
    }

    open fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) { //3 (order of execution)
        this.holder = holder
        onEdit(!(adapter.editMode.isNone))
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