package com.alteratom.dashboard.switcher

import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import com.alteratom.dashboard.activities.MainActivity.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.activities.MainActivity.FragmentManager.Animations.slideRight as slideRightAnimation

object FragmentSwitcher : Switcher() {

    fun switch(slideRight: Boolean, target: Fragment = DashboardFragment()) {
        if (G.dashboards.size < 2) return

        var index = G.dashboardIndex - if (slideRight) 1 else -1
        if (index < 0) index = G.dashboards.lastIndex
        if (index > G.dashboards.lastIndex) index = 0

        if (G.setCurrentDashboard(index)) fm.replaceWith(
            target,
            false,
            if (slideRight) slideRightAnimation else slideLeftAnimation
        )
    }

    fun handle(e: MotionEvent?) = TileSwitcher.handle(e, { r -> switch(r) })
    fun handle(e: MotionEvent?, target: Fragment) =
        TileSwitcher.handle(e, { r -> switch(r, target) })
}