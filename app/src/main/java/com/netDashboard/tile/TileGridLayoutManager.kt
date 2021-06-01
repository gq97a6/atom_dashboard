package com.netDashboard.tile

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class TileGridLayoutManager(context: Context, spanCount: Int): GridLayoutManager(context, spanCount) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return true
    }
}