package com.netDashboard.tiles

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.netDashboard.dashboard_activity.DashboardActivity
import java.util.*

class TilesAdapter(
    private val context: Context,
    private val spanCount: Int,
    mode: String = ""
) : ListAdapter<Tile, TilesAdapter.TileViewHolder>(TileDiffCallback) {

    var swapMode = false
        get() = field
        set(value) { field = value }

    var removeMode = false
        get() = field
        set(value) { field = value }

    var addMode = false
        get() = field
        set(value) { field = value }

    lateinit var tiles: MutableList<Tile>
    lateinit var currentTile: Tile

    private val tileOnClick = MutableLiveData(-1)

    fun getTileOnClickLiveData(): LiveData<Int> {
        return tileOnClick
    }

    init {
        when (mode) {
            "swap" -> swapMode = true
            "remove" -> removeMode = true
            "add" -> addMode = true
        }
    }

    override fun submitList(list: MutableList<Tile>?) {
        super.submitList(list)
        tiles = list!!.toMutableList()
    }

    override fun getCurrentList(): MutableList<Tile> {
        return tiles
    }

    override fun getItemCount(): Int {
        return tiles.size
    }

    override fun getItemId(position: Int): Long {
        return tiles[position].id!!
    }

    override fun getItemViewType(position: Int): Int {
        currentTile = tiles[position]
        return tiles[position].getItemViewType(context, spanCount) //Tile function
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        return currentTile.onCreateViewHolder(parent, viewType) //Tile function
    }

    class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        tiles[position].onBindViewHolder(holder, position) //Tile function

        holder.itemView.setOnClickListener {
            when {
                swapMode -> {
                    tiles[position].flag(!tiles[position].flag())

                    for ((i, t) in tiles.withIndex()) {

                        if (t.flag() && tiles[position].id != t.id) {
                            tiles[i].flag(false)
                            tiles[position].flag(false)

                            Collections.swap(tiles, position, i)

                            notifyItemChanged(position)
                            notifyItemChanged(i)
                        }
                    }
                }
                removeMode -> {
                    for ((i, t) in tiles.withIndex()) {

                        if (t.flag() && tiles[position].id != t.id) {
                            tiles[i].flag(false)
                        }
                    }

                    tiles[position].flag(!tiles[position].flag(), 1)
                }
                addMode -> {
                    tileOnClick.postValue(position)
                }
                else -> {
                    tiles[position].onClick()
                }
            }
        }
    }
}

object TileDiffCallback : DiffUtil.ItemCallback<Tile>() {
    override fun areItemsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.areItemsTheSame(oldItem, newItem) //Tile function
    }

    override fun areContentsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.areContentsTheSame(oldItem, newItem) //Tile function
    }
}

