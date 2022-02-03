package com.netDashboard.activities.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.ChipGroup
import com.netDashboard.R
import com.netDashboard.Theme.ColorPallet
import com.netDashboard.databinding.FragmentTileIconBinding
import com.netDashboard.globals.G.theme
import com.netDashboard.globals.G.tile
import com.netDashboard.icon.IconCompound
import com.netDashboard.icon.IconCompundAdapter
import com.netDashboard.icon.Icons.lineCats
import com.netDashboard.icon.Icons.lineIcons
import com.netDashboard.icon.Icons.solidCats
import com.netDashboard.icon.Icons.solidIcons
import com.netDashboard.icon.Icons.thinCats
import com.netDashboard.icon.Icons.thinIcons
import java.util.*

class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconCompundAdapter
    private lateinit var adapterColors: IconCompundAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTileIconBinding.inflate(inflater, container, false)
        return b.root
        b.tiIconType.setOnCheckedChangeListener(this as ChipGroup.OnCheckedChangeListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(b.root, requireContext(), true)

        setupIconsRecyclerView()
        setupColorsRecyclerView()
        viewConfig()

        b.tiIconType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                b.tiLine.id -> updateRecyclerView(lineIcons, lineCats)
                b.tiThin.id -> updateRecyclerView(thinIcons, thinCats)
                b.tiSolid.id -> updateRecyclerView(solidIcons, solidCats)
            }
        }

        b.tiHue.setOnTouchListener { v, e ->
            onHSVChange(e)
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tiSaturation.setOnTouchListener { _, e ->
            onHSVChange(e)
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tiValue.setOnTouchListener { _, e ->
            onHSVChange(e)
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }
    }

    private fun viewConfig() {
        b.tiIcon.setBackgroundResource(tile.iconRes)
        onColorChange(tile.hsv, tile.colorPallet)

        b.tiValText.tag = if (theme.a.isDark) "colorC" else "colorB"
        b.tiValue.tag = if (theme.a.isDark) "disabled" else "enabled"
        b.tiValue.isEnabled = !theme.a.isDark

        theme.apply(b.tiColor)

        if (theme.a.isDark) b.tiValue.value = 1f
        else b.tiValue.value = theme.a.hsv[2]
        b.tiHue.value = tile.hsv[0]
        b.tiSaturation.value = tile.hsv[1]
    }

    private fun onHSVChange(e: MotionEvent) {
        when (e.action) {
            MotionEvent.ACTION_DOWN ->
                (b.tcScrollView as ViewGroup?)?.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_UP ->
                (b.tcScrollView as ViewGroup?)?.requestDisallowInterceptTouchEvent(false)
        }

        val hsv = floatArrayOf(b.tiHue.value, b.tiSaturation.value, b.tiValue.value)
        val p = theme.a.getColorPallet(hsv, true)
        onColorChange(hsv, p)
    }

    private fun onColorChange(hsv: FloatArray, colorPallet: ColorPallet) {
        tile.hsv = hsv
        b.tiIcon.backgroundTintList = ColorStateList.valueOf(colorPallet.color)
        val drawable = b.tiIconFrame.background as? GradientDrawable
        drawable?.mutate()
        drawable?.setStroke(1, colorPallet.color)
        drawable?.cornerRadius = 15f
    }

    private fun setupColorsRecyclerView() {
        val spanCount = 6

        adapterColors = IconCompundAdapter(requireContext(), spanCount)
        adapterColors.setHasStableIds(true)

        adapterColors.onItemClick = { item ->
            if (item.isColorAny) {
                b.tiColor.visibility = if (b.tiColor.isVisible) GONE else VISIBLE
            } else {
                onColorChange(item.hsv, item.colorPallet)
            }
        }

        val list = mutableListOf<IconCompound>()
        adapterColors.submitList(list)

        fun addToList(hsv: FloatArray) {
            val colorPallet = theme.a.getColorPallet(hsv, true)

            list.add(
                IconCompound(
                    colorPallet = colorPallet,
                    hsv = hsv,
                    isColor = true
                )
            )
        }

        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                addToList(
                    if (theme.a.isDark) floatArrayOf(ii.toFloat(), i.toFloat() / 100, 1f)
                    else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)
                )
            }
        }

        addToList(floatArrayOf(0f, 0f, 0f))
        list.add(IconCompound(R.drawable.il_interface_question, isColorAny = true))

        val layoutManager = GridLayoutManager(requireContext(), spanCount)

        b.tiRecyclerViewColors.layoutManager = layoutManager
        b.tiRecyclerViewColors.adapter = adapterColors
    }

    private fun setupIconsRecyclerView() {
        val spanCount = 6

        adapter = IconCompundAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemClick = { item ->
            if (!item.isCategory) {
                tile.iconRes = item.res
                b.tiIcon.setBackgroundResource(item.res)
                //(activity as MainActivity).fm.popBackStack()
            }
        }

        updateRecyclerView(lineIcons, lineCats)

        class CustomLinearLayoutManager(c: Context, sc: Int) : GridLayoutManager(c, sc) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }

        val layoutManager =
            CustomLinearLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.list[position].isCategory) spanCount else 1
                }
            }

        b.tiRecyclerView.layoutManager = layoutManager
        b.tiRecyclerView.adapter = adapter

    }

    fun updateRecyclerView(icons: List<IconCompound>, cats: List<String>) {
        val list = mutableListOf<IconCompound>()
        adapter.submitList(list)

        for (c in cats) {
            val catUp =
                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            list.add(IconCompound(cat = catUp, isCategory = true))
            list.addAll(icons.filter { it.cat == c })
        }

        adapter.notifyDataSetChanged()
    }
}