package com.netDashboard.tiles

import android.graphics.Color
import com.netDashboard.tiles.tiles_types.button.ButtonTile
import com.netDashboard.tiles.tiles_types.slider.SliderTile

class TilesList {
    fun get(color: Int = Color.parseColor("#A3A3A3")): List<Tile> {
        return listOf(
            ButtonTile("Button", color, 3, 1),
            SliderTile("Slider", color, 3, 1)
        )
    }
}