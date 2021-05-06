package com.netDashboard.tiles

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.netDashboard.R
import com.netDashboard.createToast
import com.netDashboard.getContrastColor
import com.netDashboard.getScreenWidth
import java.io.Serializable
import java.util.*

abstract class Tile(
    var name: String,
    var color: Int,
    private var layout: Int,
    var width: Int,
    var height: Int
) : Serializable {

    val id: Long?

    private var editMode = false
    private var flag = false

    var spanCount = 1
    var context: Context? = null
    var holder: TilesAdapter.TileViewHolder? = null

    init {
        id = Random().nextLong()
    }

    fun getItemViewType(context: Context, spanCount: Int): Int {
        this.context = context
        this.spanCount = spanCount

        return layout
    }

    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TilesAdapter.TileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return TilesAdapter.TileViewHolder(view)
    }

    open fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        this.holder = holder

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((getScreenWidth() - view.paddingLeft * 2) / spanCount) * height
        view.layoutParams = params

        holder.itemView.setOnLongClickListener {
            if (editMode) {
                createToast(context!!, "open settings! ${holder.adapterPosition}")
            }

            return@setOnLongClickListener true
        }

        editMode(editMode)
        flag(flag)
    }

    fun areItemsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.id == newItem.id
    }

    open fun editMode(isEnabled: Boolean) {
        editMode = isEnabled
    }

    fun editMode(): Boolean {
        return editMode
    }

    open fun flag(flag: Boolean, type: Int = 0) {
        this.flag = flag

        val flagMark = holder?.itemView?.findViewById<ImageView>(R.id.swapReady)

        when (type) {
            0 -> flagMark?.setBackgroundResource(R.drawable.icon_swap)
            1 -> flagMark?.setBackgroundResource(R.drawable.icon_remove)
        }

        if (flag) {
            flagMark?.backgroundTintList = ColorStateList.valueOf(getContrastColor(color))
            flagMark?.visibility = View.VISIBLE
        } else {
            flagMark?.visibility = View.GONE
        }
    }

    fun flag(): Boolean {
        return flag
    }

    open fun setThemeColor(color: Int) {}
}