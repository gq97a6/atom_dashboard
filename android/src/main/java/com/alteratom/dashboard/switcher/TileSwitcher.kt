package com.alteratom.dashboard.switcher

import android.view.MotionEvent
import androidx.compose.ui.input.pointer.PointerEvent
import com.alteratom.dashboard.activity.fragment.TilePropertiesFragment
import com.alteratom.dashboard.objects.FragmentManager.fm
import com.alteratom.dashboard.objects.G.dashboard
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.objects.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.objects.FragmentManager.Animations.slideRight as slideRightAnimation

object TileSwitcher : Switcher() {

    fun switch(slideRight: Boolean) {
        if (dashboard.tiles.size < 2) return

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

    fun handle(e: MotionEvent?) = handle(e) { r -> switch(r) }
}