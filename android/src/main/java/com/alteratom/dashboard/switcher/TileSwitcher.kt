package com.alteratom.dashboard.switcher

import android.view.MotionEvent
import com.alteratom.dashboard.fragment.TilePropertiesFragment
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.slideRight as slideRightAnimation

object TileSwitcher : Switcher() {

    fun switch(slideRight: Boolean) {
        if (aps.dashboard.tiles.size < 2) return

        var index = aps.dashboard.tiles.indexOf(aps.tile) - if (slideRight) 1 else -1
        if (index < 0) index = aps.dashboard.tiles.lastIndex
        if (index > aps.dashboard.tiles.lastIndex) index = 0

        aps.tile = aps.dashboard.tiles[index]

        fm.replaceWith(
            TilePropertiesFragment(),
            false,
            if (slideRight) slideRightAnimation else slideLeftAnimation
        )
    }

    fun handle(e: MotionEvent?) = handle(e) { r -> switch(r) }
}