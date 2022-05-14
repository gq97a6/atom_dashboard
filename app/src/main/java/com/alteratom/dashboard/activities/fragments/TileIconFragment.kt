package com.alteratom.dashboard.activities.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.icon.IconAdapter
import com.alteratom.databinding.FragmentTileIconBinding


class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentTileIconBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(b.root, requireContext(), true)
        viewConfig()

        setupRecyclerView()
    }

    private fun viewConfig() {
        b.tiIcon.setBackgroundResource(getIconRes())
        onColorChange(getIconColorPallet())
    }

    private fun onColorChange(colorPallet: Theme.ColorPallet) {
        b.tiIcon.backgroundTintList = ColorStateList.valueOf(colorPallet.color)
        val drawable = b.tiIconFrame.background as? GradientDrawable
        //drawable?.mutate()
        drawable?.setStroke(1, colorPallet.color)
        drawable?.cornerRadius = 15f
    }

    private fun setupRecyclerView() {
        val spanCount = 6

        adapter = IconAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)
        adapter.applyIconSet("l")

        adapter.onColorChange = { _, colorPallet ->
            onColorChange(colorPallet)
        }

        adapter.onIconChange = {
            b.tiIcon.setBackgroundResource(it)
        }

        class CustomGridLayoutManager(c: Context, sc: Int) : GridLayoutManager(c, sc) {
            override fun supportsPredictiveItemAnimations(): Boolean = false
        }

        val layoutManager =
            CustomGridLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    adapter.list[position].spanCount.let {
                        return if (it == -1) spanCount else it
                    }
                }
            }

        b.tiRecyclerView.layoutManager = layoutManager
        b.tiRecyclerView.adapter = adapter
    }
}