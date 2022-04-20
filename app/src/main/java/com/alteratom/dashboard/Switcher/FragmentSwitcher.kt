package com.alteratom.dashboard.DashboardSwitcher

import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G
import com.alteratom.dashboard.Switcher
import com.alteratom.dashboard.TileSwitcher.TileSwitcher
import com.alteratom.dashboard.activities.fragments.DashboardFragment

object FragmentSwitcher : Switcher() {

    fun switch(slideRight: Boolean, target: Fragment = DashboardFragment()) {
        var index = G.dashboardIndex - if (slideRight) 1 else -1
        if (index < 0) index = G.dashboards.lastIndex
        if (index > G.dashboards.lastIndex) index = 0

        if (G.setCurrentDashboard(index)) activity.fm.replaceWith(
            target, false, true, slideRight
        )
    }

    fun handle(e: MotionEvent?) = TileSwitcher.handle(e, { r -> switch(r) })
    fun handle(e: MotionEvent?, target: Fragment) =
        TileSwitcher.handle(e, { r -> switch(r, target) })
}