package com.netDashboard.icon

import android.content.Context
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.slider.Slider
import com.netDashboard.R
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.globals.G.theme
import com.netDashboard.icon.Icons.lineCats
import com.netDashboard.icon.Icons.lineIcons
import com.netDashboard.icon.Icons.solidCats
import com.netDashboard.icon.Icons.solidIcons
import com.netDashboard.icon.Icons.thinCats
import com.netDashboard.icon.Icons.thinIcons
import com.netDashboard.recycler_view.RecyclerViewAdapter
import java.util.*


class IconAdapter(context: Context, spanCount: Int) :
    RecyclerViewAdapter<Icon>(context, spanCount, DiffCallback) {

    private var iconCount = 0
    var onColorChange: (FloatArray, ColorPallet) -> Unit = { hsv, colorPallet -> }
    var onIconChange: (Int) -> Unit = {}

    object DiffCallback : DiffUtil.ItemCallback<Icon>() {
        override fun areItemsTheSame(
            oldItem: Icon,
            newItem: Icon
        ): Boolean {
            return oldItem.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(
            oldItem: Icon,
            newItem: Icon
        ): Boolean {
            return oldItem.areContentsTheSame(oldItem, newItem)
        }
    }

    init {
        list.add(IconGap())

        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                val hsv = if (theme.a.isDark) floatArrayOf(ii.toFloat(), i.toFloat() / 100, 1f)
                else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)
                val colorPallet = theme.a.getColorPallet(hsv, true)

                list.add(IconColor(hsv, colorPallet))
            }
        }

        val hsv = floatArrayOf(0f, 0f, 0f)
        val colorPallet = theme.a.getColorPallet(hsv, true)

        list.add(IconColor(hsv, colorPallet))
        list.add(IconColorAny())

        list.add(IconColorPicker())
        list.add(IconBar())
    }

    fun setColorPicker(hsv: FloatArray) {
        val picker = list.find { it is IconColorPicker } as IconColorPicker
        picker.apply {
            val h = holder?.itemView?.findViewById<Slider>(R.id.iicp_hue)
            val s = holder?.itemView?.findViewById<Slider>(R.id.iicp_saturation)
            val v = holder?.itemView?.findViewById<Slider>(R.id.iicp_value)

            h?.value = hsv[0]
            s?.value = hsv[1]
            v?.value = hsv[2]
        }
    }

    fun applyIconSet(type: String) {
        var icons: List<IconIcon> = listOf()
        var cats: List<String> = listOf()

        when (type) {
            "l" -> {
                icons = lineIcons
                cats = lineCats
            }
            "t" -> {
                icons = thinIcons
                cats = thinCats
            }
            "s" -> {
                icons = solidIcons
                cats = solidCats
            }
        }


        list.subList(list.size - iconCount, list.size).clear()
        val size = list.size

        for (c in cats) {
            val catUp =
                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            list.add(IconCategory(catUp))
            list.addAll(icons.filter { it.cat == c })
        }

        iconCount = list.size - size

        notifyDataSetChanged()
    }
}

