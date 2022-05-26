package com.alteratom.dashboard.icon

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.alteratom.R
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.google.android.material.slider.Slider
import java.util.*

class IconAdapter(context: Context, spanCount: Int) :
    RecyclerViewAdapter<Icon>(context, spanCount) {

    private var iconCount = 0
    var onColorChange: (FloatArray, ColorPallet) -> Unit = { _, _ -> }
    var onIconChange: (Int) -> Unit = {}

    init {
        list.add(IconGap())

        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                val hsv = if (theme.a.isDark) floatArrayOf(ii.toFloat(), i.toFloat() / 100, 1f)
                else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)
                val colorPallet = theme.a.getColorPallet(hsv, true)

                list.add(IconColor(1, hsv, colorPallet))
            }
        }

        val hsv = floatArrayOf(0f, 0f, 0f)
        val colorPallet = theme.a.getColorPallet(hsv, true)

        list.add(IconColor(3, hsv, colorPallet))
        list.add(IconColorAny())
    }

    fun applyIconSet(type: String) {
        val icons = Icons.icons.values.filter { it.type == type }

        list.subList(list.size - iconCount, list.size).clear()
        val size = list.size

        for (c in Icons.cats) {
            val catUp =
                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            list.add(IconCategory(catUp))
            list.addAll(icons.filter { it.cat == c })
        }

        iconCount = list.size - size

        notifyDataSetChanged()
    }
}

