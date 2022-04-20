package com.alteratom.dashboard

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import com.alteratom.dashboard.activities.MainActivity
import kotlin.math.abs

abstract class Switcher {
    lateinit var activity: MainActivity
    private var flingTouchdownX = 0f
    private var flingTouchdownY = 0f

    fun handle(e: MotionEvent?, onSwitch: (r: Boolean) -> Unit): Boolean {
        if (e != null) {
            when (e.action) {
                KeyEvent.ACTION_DOWN -> {
                    flingTouchdownX = e.x
                    flingTouchdownY = e.y
                }
                KeyEvent.ACTION_UP -> {
                    val flingLen = (flingTouchdownX - e.x) / screenWidth

                    if (abs(flingTouchdownY - e.y) < 120 &&
                        abs(flingLen) > 0.15 &&
                        e.eventTime - e.downTime in 30..300
                    ) {
                        onSwitch(flingLen < 0)
                        return true
                    }
                }
            }
        }

        return false
    }
}