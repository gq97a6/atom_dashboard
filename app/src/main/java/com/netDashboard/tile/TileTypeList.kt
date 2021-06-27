package com.netDashboard.tile

import android.graphics.Color
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.slider.SliderTile

class TileTypeList {
    fun get(color: Int = Color.parseColor("#A3A3A3")): List<Tile> {
        return listOf(
            ButtonTile("button", color, 3, 1),
            SliderTile("slider", color, 3, 1)
        )
    }

    fun getById(id: Int, color: Int = Color.parseColor("#A3A3A3")): Tile {
        return get(color)[id]
    }

    fun getDefault(color: Int = Color.parseColor("#A3A3A3")): List<Tile> {
        return listOf(
            //ButtonTile("", color, 1, 1),
            //SliderTile("", color, 1, 1)
        )
    }
}