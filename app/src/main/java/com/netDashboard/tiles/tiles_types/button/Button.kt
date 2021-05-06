package com.netDashboard.tiles.tiles_types.button

import android.content.res.ColorStateList
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.createToast
import com.netDashboard.getContrastColor
import com.netDashboard.getRandomColor
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter
import java.util.*

class ButtonTile(name: String, color: Int, x: Int, y: Int) :
    Tile(name, color, R.layout.button_tile, x, y) {

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        setThemeColor(color)

        holder.itemView.findViewById<Button>(R.id.button).setOnClickListener {
            holder.itemView.callOnClick()

            if (!editMode()) {
                setThemeColor(getRandomColor())
            }

            holder.itemView.findViewById<Button>(R.id.button).setOnLongClickListener {
                if (editMode()) {
                    createToast(context!!, "open settings! ${holder.adapterPosition}")
                }

                return@setOnLongClickListener true
            }
        }
    }

    override fun setThemeColor(color: Int) {
        this.color = color

        holder?.itemView?.findViewById<Button>(R.id.button)?.backgroundTintList =
            ColorStateList.valueOf(
                color
            )

        holder?.itemView?.findViewById<Button>(R.id.button)?.setTextColor(getContrastColor(color))
    }
}