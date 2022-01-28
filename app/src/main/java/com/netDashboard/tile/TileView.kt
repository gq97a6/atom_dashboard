package com.netDashboard.tile

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.netDashboard.globals.G.theme

class TileView(
    context: Context,
    attrs: AttributeSet
) : FrameLayout(context, attrs) {
    var colorPallet = theme.a.colorPallet
}