package com.alteratom.dashboard.switcher

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.screenWidth
import kotlin.math.abs

abstract class Switcher {
    lateinit var activity: MainActivity

    private var touchdownX = 0f
    private var touchdownY = 0f

    private var touchdownMillis = 0L
    private var touchdownOffset = Offset(0f, 0f)

    protected fun handle(e: MotionEvent?, onSwitch: (r: Boolean) -> Unit): Boolean {
        if (e != null) {
            when (e.action) {
                KeyEvent.ACTION_DOWN -> {
                    touchdownX = e.x
                    touchdownY = e.y
                }

                KeyEvent.ACTION_UP -> {
                    val flingLen = (touchdownX - e.x) / screenWidth
                    if (abs(touchdownY - e.y) < 250 &&
                        abs(flingLen) > 0.05 &&
                        e.eventTime - e.downTime in 20..300
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