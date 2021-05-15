package com.netDashboard.tiles

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class TilesAnimator : DefaultItemAnimator() {

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder,
        newHolder: RecyclerView.ViewHolder,
        preInfo: ItemHolderInfo,
        postInfo: ItemHolderInfo
    ): Boolean {
        val oldHolderAlpha = oldHolder.itemView.alpha
        val newHolderAlpha = newHolder.itemView.alpha

        val toReturn = super.animateChange(oldHolder, newHolder, preInfo, postInfo)

        oldHolder.itemView.alpha = oldHolderAlpha
        newHolder.itemView.alpha = newHolderAlpha

        return toReturn
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
        val oldHolderAlpha = oldHolder?.itemView?.alpha
        val newHolderAlpha = newHolder?.itemView?.alpha

        val toReturn = super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)

        oldHolder?.itemView?.alpha = oldHolderAlpha ?: 1f
        newHolder?.itemView?.alpha = newHolderAlpha ?: 1f

        return toReturn
    }
}