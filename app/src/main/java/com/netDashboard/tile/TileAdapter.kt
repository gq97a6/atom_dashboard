package com.netDashboard.tile

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

class TilesAdapter(
    private val context: Context,
    private val spanCount: Int,
    mode: String = ""
) : ListAdapter<Tile, TilesAdapter.TileViewHolder>(TileDiffCallback) {

    var swapMode = false
    var swapModeLock = false

    var removeMode = false
    private var addMode = false

    lateinit var tiles: MutableList<Tile>
    private lateinit var currentTile: Tile

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
                addMode -> {
                    tileOnClick.postValue(position)
                }
                else -> {
                    tiles[position].onClick()
                }
            }
        }
    }

    private fun swapTiles(position: Int) {

        if (!swapModeLock) {
            if (!tiles[position].swapLock) {
                tiles[position].flag(!tiles[position].flag())
            }

            for ((pos, t) in tiles.withIndex()) {

                if (t.flag() && tiles[position].id != t.id) {
                    val recyclerView =
                        tiles[position].holder?.itemView?.parent as RecyclerView

                    recyclerView.itemAnimator?.changeDuration = 0

                    val layoutManager = recyclerView.layoutManager as GridLayoutManager

                    val tileHeight = tiles[position].holder?.itemView?.height!!
                    val max = (recyclerView.height / tileHeight) * spanCount
                    val f = layoutManager.findFirstVisibleItemPosition()
                    val l = layoutManager.findLastVisibleItemPosition()

                    tiles[pos].flag(false)
                    tiles[position].flag(false)

                    if (abs(position - pos + 1) <= max && position in f..l && pos in f..l) {
                        swapModeLock = true

                        recyclerView.suppressLayout(true)

                        tiles[pos].swapLock = true
                        tiles[position].swapLock = true

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
                        val duration = (distance * 0.7).toLong()

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
                            tiles[pos].swapLock = false
                            tiles[position].swapLock = false

                            Collections.swap(tiles, position, pos)
                            notifyItemChanged(position)
                            notifyItemChanged(pos)

                            tiles[pos].holder?.itemView?.elevation = 0f
                            tiles[position].holder?.itemView?.elevation = 0f

                            var noneIsMoving = true
                            for (tt in tiles) {
                                if (tt.swapLock) noneIsMoving = false; break
                            }

                            if (noneIsMoving) {
                                recyclerView.suppressLayout(false)
                            }

                            swapModeLock = false
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

            if (t.flag() && tiles[position].id != t.id) {
                tiles[i].flag(false)
            }
        }

        tiles[position].flag(!tiles[position].flag(), 1)
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

