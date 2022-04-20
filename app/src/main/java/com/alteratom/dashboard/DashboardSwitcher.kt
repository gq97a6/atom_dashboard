package com.alteratom.dashboard

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import kotlin.math.abs

object DashboardSwitcher {
    private var flingTouchdownX = 0f
    private var flingTouchdownY = 0f

    fun switchDashboard(slideRight: Boolean, activity: FragmentActivity, target: Fragment = DashboardFragment()) {
        var index = G.dashboardIndex - if (slideRight) 1 else -1
        if (index < 0) index = G.dashboards.lastIndex
        if (index > G.dashboards.lastIndex) index = 0

        if (G.setCurrentDashboard(index)) (activity as MainActivity).fm.replaceWith(
            target, false, true, slideRight
        )
    }

    fun handle(e: MotionEvent?, activity: FragmentActivity, target: Fragment = DashboardFragment()) {
        if (e != null) {
            when (e.action) {
                KeyEvent.ACTION_DOWN -> {
                    flingTouchdownX = e.x
                    flingTouchdownY = e.y
                }
                KeyEvent.ACTION_UP -> {
                    val flingLen = (flingTouchdownX - e.x) / screenWidth

                    if (abs(flingTouchdownY - e.y) < 120 &&
                        abs(flingLen) > 0.16 &&
                        e.eventTime - e.downTime in 30..300
                    ) {
                        switchDashboard(flingLen < 0, activity, target)
                    }
                }
            }
        }
    }
}