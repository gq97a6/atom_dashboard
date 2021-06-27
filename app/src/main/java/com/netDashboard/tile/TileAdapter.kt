package com.netDashboard.tile

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.netDashboard.activities.dashboard.config_new_tile.ConfigTileActivity
import com.netDashboard.createNotification
import java.util.*
import kotlin.math.abs
import kotlin.math.pow

class TilesAdapter(
    private val context: Context,
    private val spanCount: Int,
    private var mode: String = "",
    private val dashboardName: String = ""
) : ListAdapter<Tile, TilesAdapter.TileViewHolder>(TileDiffCallback) {

    var isEdit
        get() = swapMode || removeMode || addMode || editMode
        set(value) {
            mode = ""

            if (value) {
                mode = "edit"
                swapLock = false
            }

            for (t in tiles) {
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

            for (t in tiles) {
                t.flag()
            }
        }

    }

    var swapMode
        get() = mode == "swap"
        set(value) {
            mode("swap", value)
        }

    var removeMode
        get() = mode == "remove"
        set(value) {
            mode("remove", value)
        }

    var editMode
        get() = mode == "edit"
        set(value) {
            mode("edit", value)
        }

    private var addMode
        get() = mode == "add"
        set(value) {
            mode("add", value)
        }

    private var swapLock = false

    lateinit var tiles: MutableList<Tile>
    private lateinit var currentTile: Tile

    private val tileOnClick = MutableLiveData(-1)

    fun getTileOnClickLiveData(): LiveData<Int> {
        return tileOnClick
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
        return tiles[position].getItemViewType(context, spanCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TileViewHolder {
        return currentTile.onCreateViewHolder(parent, viewType)
    }

    class TileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onBindViewHolder(holder: TileViewHolder, position: Int) {
        tiles[position].onBindViewHolder(holder, position)

        holder.itemView.setOnClickListener {
            when {
                swapMode -> {
                    swapTiles(position)
                }
                removeMode -> {
                    removeTile(position)
                }
                editMode -> {
                    Intent(context, ConfigTileActivity::class.java).also {
                        it.putExtra("tileId", 12)
                        it.putExtra("dashboardName", dashboardName)
                        (context as Activity).overridePendingTransition(0, 0)
                        context.startActivity(it)
                        context.finish()
                    }
                }
                addMode -> {
                    tileOnClick.postValue(position)
                }
                else -> {
                    tiles[position].onClick()
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            createNotification(context, "longClick", "performed")
            return@setOnLongClickListener true
        }
    }

    private fun swapTiles(position: Int) {

        if (!swapLock) {

            if (tiles[position].flag != "lock") {
                tiles[position].toggleFlag("swap")
            }

            for ((pos, t) in tiles.withIndex()) {

                if (t.flag == "swap" && tiles[position].id != t.id) {
                    val recyclerView =
                        tiles[position].holder?.itemView?.parent as RecyclerView

                    recyclerView.itemAnimator?.changeDuration = 0

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager

                    val tileHeight = tiles[position].holder?.itemView?.height!!
                    val max = (recyclerView.height / tileHeight) * spanCount
                    val f = layoutManager.findFirstVisibleItemPosition()
                    val l = layoutManager.findLastVisibleItemPosition()

                    tiles[pos].flag()
                    tiles[position].flag()

                    if (abs(position - pos + 1) <= max && position in f..l && pos in f..l) {
                        swapLock = true

                        recyclerView.suppressLayout(true)

                        tiles[pos].flag("lock")
                        tiles[position].flag("lock")

                        val xyA = IntArray(2)
                        tiles[pos].holder?.itemView?.getLocationOnScreen(xyA)

                        val xyB = IntArray(2)
                        tiles[position].holder?.itemView?.getLocationOnScreen(xyB)

                        tiles[pos].holder?.itemView?.elevation = 2f
                        tiles[position].holder?.itemView?.elevation = 1f

                        val xA = tiles[pos].holder?.itemView?.x!!
                        val xB = tiles[position].holder?.itemView?.x!!

                        val yA = tiles[pos].holder?.itemView?.y!!
                        val yB = tiles[position].holder?.itemView?.y!!

                        val distance = kotlin.math.sqrt(
                            (xA - xB).toDouble().pow(2) + (yA - yB).toDouble().pow(2)
                        )
                        val duration = (distance * 0.5).toLong()

                        tiles[pos].holder?.itemView?.animate()?.cancel()

                        tiles[pos].holder?.itemView?.animate()
                            ?.x(xB)
                            ?.y(yB)
                            ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                            duration

                        tiles[position].holder?.itemView?.animate()
                            ?.x(xA)
                            ?.y(yA)
                            ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                            duration

                        Handler(Looper.getMainLooper()).postDelayed({
                            recyclerView.suppressLayout(false)

                            tiles[pos].flag()
                            tiles[position].flag()

                            Collections.swap(tiles, position, pos)
                            notifyItemChanged(position)
                            notifyItemChanged(pos)

                            tiles[pos].holder?.itemView?.elevation = 0f
                            tiles[position].holder?.itemView?.elevation = 0f

                            swapLock = false
                        }, duration + 50)
                    } else {
                        Collections.swap(tiles, position, pos)
                        notifyItemChanged(position)
                        notifyItemChanged(pos)
                    }
                }
            }
        }
    }

    private fun removeTile(position: Int) {
        val recyclerView =
            tiles[position].holder?.itemView?.parent as RecyclerView

        recyclerView.itemAnimator?.changeDuration = 250

        for ((i, t) in tiles.withIndex()) {

            if (t.flag == "remove" && tiles[position].id != t.id) {
                tiles[i].flag()
            }
        }

        tiles[position].toggleFlag("remove")
    }
}

object TileDiffCallback : DiffUtil.ItemCallback<Tile>() {
    override fun areItemsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.areItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.areContentsTheSame(oldItem, newItem)
    }
}

