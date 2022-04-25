package com.alteratom.dashboard.color_picker

import android.graphics.Paint

data class Paints(
    val shaderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG),
    val indicatorPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
)