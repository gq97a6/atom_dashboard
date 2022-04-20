package com.alteratom.dashboard.TileSwitcher

import android.view.MotionEvent
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Switcher
import com.alteratom.dashboard.activities.fragments.TilePropertiesFragment

object TileSwitcher : Switcher() {

    fun switch(slideRight: Boolean) {
        var index = dashboard.tiles.indexOf(tile) - if (slideRight) 1 else -1
        if (index < 0) index = dashboard.tiles.lastIndex
        if (index > G.dashboard.tiles.lastIndex) index = 0

        run {}

        tile = dashboard.tiles[index]

        activity.fm.replaceWith(
            TilePropertiesFragment(), false, true, slideRight
        )
    }

    fun handle(e: MotionEvent?) = TileSwitcher.handle(e, { r -> switch(r) })
}