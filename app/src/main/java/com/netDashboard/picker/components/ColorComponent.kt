package com.netDashboard.picker.components

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Shader
import android.view.MotionEvent
import android.view.MotionEvent.*
import com.netDashboard.picker.Metrics
import com.netDashboard.picker.Paints
import com.netDashboard.picker.listeners.OnColorSelectionListener

internal abstract class ColorComponent(val metrics: Metrics, val paints: Paints) {
}