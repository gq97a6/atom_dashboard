package com.netDashboard.tiles.tiles_types.slider

import com.google.android.material.slider.Slider
import com.netDashboard.R
import com.netDashboard.tiles.Tile
import com.netDashboard.tiles.TilesAdapter

class SliderTile(name: String, color: Int, x: Int, y: Int) :
    Tile(name, color, R.layout.slider_tile, x, y) {

    override fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        holder.itemView.findViewById<Slider>(R.id.slider).isEnabled = !editMode()

        setThemeColor(color)
    }

    override fun editMode(isEnabled: Boolean) {
        super.editMode(isEnabled)

        holder?.itemView?.findViewById<Slider>(R.id.slider)?.isEnabled = !editMode()
    }
}