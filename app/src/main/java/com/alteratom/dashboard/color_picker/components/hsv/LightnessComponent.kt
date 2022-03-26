package com.alteratom.dashboard.color_picker.components.hsv

import android.graphics.Color.HSVToColor
import com.alteratom.dashboard.color_picker.Metrics
import com.alteratom.dashboard.color_picker.Paints
import com.alteratom.dashboard.color_picker.components.ArcComponent

internal class LightnessComponent(
    metrics: Metrics,
    paints: Paints,
    arcLength: Float,
    arcStartAngle: Float
) : ArcComponent(metrics, paints, arcLength, arcStartAngle) {

    override val componentIndex: Int = 2
    override val range: Float = 1f
    override val noOfColors = 2
    override val colors = IntArray(noOfColors)
    override val colorPosition = FloatArray(noOfColors)

    override fun updateColorArray(color: FloatArray): IntArray {
        for (i in 0 until noOfColors) {
            color[componentIndex] = i.toFloat() / (noOfColors - 1)
            colors[i] = HSVToColor(color)
        }
        return colors
    }
}