package com.alteratom.dashboard.switcher

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.screenWidth
import kotlin.math.abs

abstract class Switcher {
    lateinit var activity: MainActivity

    private var touchdownX = 0f
    private var touchdownY = 0f

    private var touchdownMillis = 0L
    private var touchdownOffset = Offset(0f, 0f)

    protected fun handle(e: PointerEvent, onSwitch: (r: Boolean) -> Unit): Boolean {
        when (e.type) {
            PointerEventType.Press -> {
                touchdownOffset = e.changes[0].position
                touchdownMillis = e.changes[0].uptimeMillis
            }
            PointerEventType.Release -> {
                val flingLen = (touchdownOffset.x - e.changes[0].position.x) / screenWidth
                val a = abs(touchdownOffset.y - e.changes[0].position.y)
                val b = e.changes[0].uptimeMillis - touchdownMillis

                if (abs(touchdownOffset.y - e.changes[0].position.y) < 120 &&
                    abs(flingLen) > 0.15 &&
                    e.changes[0].uptimeMillis - touchdownMillis in 30..300
                ) {
                    onSwitch(flingLen < 0)
                    return true
                }
            }
        }

        return false
    }

    protected fun handle(e: MotionEvent?, onSwitch: (r: Boolean) -> Unit): Boolean {
        if (e != null) {
            when (e.action) {
                KeyEvent.ACTION_DOWN -> {
                    touchdownX = e.x
                    touchdownY = e.y
                }
                KeyEvent.ACTION_UP -> {
                    val flingLen = (touchdownX - e.x) / screenWidth

                    if (abs(touchdownY - e.y) < 120 &&
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