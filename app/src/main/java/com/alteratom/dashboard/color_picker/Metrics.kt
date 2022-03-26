package com.alteratom.dashboard.color_picker

import android.graphics.Color.HSVToColor
import android.graphics.Paint

internal class Metrics(
    var centerX: Float = 0f,
    var centerY: Float = 0f,
    var color: FloatArray,
    val density: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Metrics

        if (centerX != other.centerX) return false
        if (centerY != other.centerY) return false
        if (!color.contentEquals(other.color)) return false
        if (density != other.density) return false

        return true
    }

    override fun hashCode(): Int {
        var result = centerX.hashCode()
        result = 31 * result + centerY.hashCode()
        result = 31 * result + color.contentHashCode()
        result = 31 * result + density.hashCode()
        return result
    }

    fun getColor() = HSVToColor(color)

}

data class Paints(
    val shaderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG),
    val indicatorPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
)