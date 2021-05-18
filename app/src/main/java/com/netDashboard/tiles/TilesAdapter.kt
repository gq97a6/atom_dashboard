package com.netDashboard.tiles

import android.animation.ObjectAnimator
import android.content.ClipData.Item
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.netDashboard.toPx
import java.lang.Math.pow
import java.util.*
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
                    if(!swapModeLock) {
                        tiles[position].flag(!tiles[position].flag())

                        for ((pos, t) in tiles.withIndex()) {

                            if (t.flag() && tiles[position].id != t.id) {
                                //swapModeLock = true

                                tiles[pos].flag(false)
                                tiles[position].flag(false)

                                if(tiles[pos].holder?.itemView?.isShown == true && tiles[pos].holder?.itemView?.isShown == true) {

                                    val xyA = IntArray(2)
                                    tiles[pos].holder?.itemView?.getLocationOnScreen(xyA)

                                    val xyB = IntArray(2)
                                    tiles[position].holder?.itemView?.getLocationOnScreen(xyB)

                                    tiles[pos].holder?.itemView?.elevation = 20f
                                    tiles[position].holder?.itemView?.elevation = 10f

                                    val xA = xyA[0].toFloat()
                                    val xB = xyB[0].toFloat()

                                    val yA = xyA[1].toFloat() - 80.toPx()
                                    val yB = xyB[1].toFloat() - 80.toPx()

                                    val distance = kotlin.math.sqrt(
                                        (xA - xB).toDouble().pow(2) + (yA - yB).toDouble().pow(2)
                                    )
                                    val duration = (distance * 1).toLong()

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

                                        Collections.swap(tiles, position, pos)
                                        notifyItemChanged(position)
                                        notifyItemChanged(pos)

                                        tiles[pos].holder?.itemView?.elevation = 0f
                                        tiles[position].holder?.itemView?.elevation = 0f

                                        swapModeLock = false
                                    }, duration)
                                } else {
                                    Collections.swap(tiles, position, pos)
                                    notifyItemChanged(position)
                                    notifyItemChanged(pos)
                                }
                            }
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

