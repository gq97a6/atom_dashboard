package com.netDashboard.tiles

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class TilesGridLayoutManager(context: Context, spanCount: Int): GridLayoutManager(context, spanCount) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return true
    }
}