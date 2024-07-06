package com.alteratom.dashboard.switcher

import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.slideRight as slideRightAnimation

object FragmentSwitcher : Switcher() {

    fun switch(slideRight: Boolean, target: Fragment) {
        if (aps.dashboards.size < 2) return

        var index = aps.dashboardIndex - if (slideRight) 1 else -1
        if (index < 0) index = aps.dashboards.lastIndex
        if (index > aps.dashboards.lastIndex) index = 0

        if (aps.setCurrentDashboard(index)) fm.replaceWith(
            target,
            false,
            if (slideRight) slideRightAnimation else slideLeftAnimation
        )
    }

    fun handle(e: MotionEvent?, target: Fragment) = handle(e) { r -> switch(r, target) }
}