package com.netDashboard.activities.fragments

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.ChipGroup
import com.netDashboard.R
import com.netDashboard.databinding.FragmentTileIconBinding
import com.netDashboard.globals.G.theme
import com.netDashboard.globals.G.tile
import com.netDashboard.icon.IconPropertiesDrawable
import com.netDashboard.icon.IconPropertiesDrawableAdapter
import com.netDashboard.icon.Icons.lineCats
import com.netDashboard.icon.Icons.lineIcons
import com.netDashboard.icon.Icons.solidCats
import com.netDashboard.icon.Icons.solidIcons
import com.netDashboard.icon.Icons.thinCats
import com.netDashboard.icon.Icons.thinIcons
import java.util.*

class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconPropertiesDrawableAdapter
    private lateinit var adapterColors: IconPropertiesDrawableAdapter

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
        //tile = dashboard.tiles[arguments?.getInt("index", 0) ?: 0]
        setupIconsRecyclerView()
        setupColorsRecyclerView()

        b.tiIcon.backgroundTintList = ColorStateList.valueOf(tile.colorPallet.color)
        b.tiIcon.setBackgroundResource(tile.iconRes)
        val drawable = b.tiIconFrame.background as? GradientDrawable
        drawable?.mutate()
        drawable?.setStroke(1, tile.colorPallet.color)
        drawable?.cornerRadius = 15f

        b.tiIconType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                b.tiLine.id -> updateRecyclerView(lineIcons, lineCats)
                b.tiThin.id -> updateRecyclerView(thinIcons, thinCats)
                b.tiSolid.id -> updateRecyclerView(solidIcons, solidCats)
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupColorsRecyclerView() {
        val spanCount = 6

        adapterColors = IconPropertiesDrawableAdapter(requireContext(), spanCount)
        adapterColors.setHasStableIds(true)

        adapterColors.onItemClick = { item ->
            tile.hsv = item.hsv
            b.tiIcon.backgroundTintList = ColorStateList.valueOf(item.colorPallet.color)

            val drawable = b.tiIconFrame.background as? GradientDrawable
            drawable?.mutate()
            drawable?.setStroke(1, item.colorPallet.color)
            drawable?.cornerRadius = 15f
        }

        val list = mutableListOf<IconPropertiesDrawable>()
        adapterColors.submitList(list)

        fun addToList(hsv: FloatArray) {
            val colorPallet = theme.a.getColorPallet(hsv, true)

            list.add(
                IconPropertiesDrawable(
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

        val layoutManager = GridLayoutManager(requireContext(), spanCount)

        b.tiRecyclerViewColors.layoutManager = layoutManager
        b.tiRecyclerViewColors.adapter = adapterColors
    }

    private fun setupIconsRecyclerView() {
        val spanCount = 6

        adapter = IconPropertiesDrawableAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemClick = { item ->
            if (!item.isCategory) {
                tile.iconRes = item.res
                b.tiIcon.setBackgroundResource(item.res)
                //(activity as MainActivity).fm.popBackStack()
            }
        }

        updateRecyclerView(lineIcons, lineCats)

        val layoutManager =
            GridLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.list[position].isCategory) spanCount else 1
                }
            }

        b.tiRecyclerView.layoutManager = layoutManager
        b.tiRecyclerView.adapter = adapter
    }

    fun updateRecyclerView(icons: List<IconPropertiesDrawable>, cat: List<String>) {
        val list = mutableListOf<IconPropertiesDrawable>()
        adapter.submitList(list)

        for (c in cat) {
            val catUp =
                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            list.add(IconPropertiesDrawable(cat = catUp, isCategory = true))
            list.addAll(icons.filter { it.cat == c })
        }
    }
}