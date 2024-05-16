package com.alteratom.dashboard.switcher

import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.objects.FragmentManager.fm
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.FragmentManager.Animations.slideLeft as slideLeftAnimation
import com.alteratom.dashboard.objects.FragmentManager.Animations.slideRight as slideRightAnimation

object FragmentSwitcher : Switcher() {

    fun switch(slideRight: Boolean, target: Fragment) {
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

    fun handle(e: MotionEvent?, target: Fragment) = handle(e) { r -> switch(r, target) }
}