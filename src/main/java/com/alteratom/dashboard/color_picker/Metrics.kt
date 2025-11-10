package com.alteratom.dashboard.color_picker

import android.graphics.Color.HSVToColor

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
        return density == other.density
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