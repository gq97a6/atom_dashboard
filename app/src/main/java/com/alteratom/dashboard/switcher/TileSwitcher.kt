package com.alteratom.dashboard.TileSwitcher

import android.view.MotionEvent
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Switcher
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.TilePropertiesFragment
import com.alteratom.dashboard.activities.MainActivity.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.activities.MainActivity.FragmentManager.Animations.slideRight as slideRightAnimation

object TileSwitcher : Switcher() {

    fun switch(slideRight: Boolean) {
        var index = dashboard.tiles.indexOf(tile) - if (slideRight) 1 else -1
        if (index < 0) index = dashboard.tiles.lastIndex
        if (index > dashboard.tiles.lastIndex) index = 0

        tile = dashboard.tiles[index]

        fm.replaceWith(
            TilePropertiesFragment(),
            false,
            if (slideRight) slideRightAnimation else slideLeftAnimation
        )
    }

    fun handle(e: MotionEvent?) = TileSwitcher.handle(e, { r -> switch(r) })
}