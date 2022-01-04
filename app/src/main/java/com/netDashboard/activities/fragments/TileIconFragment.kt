package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.ChipGroup
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.databinding.FragmentTileIconBinding
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.theme
import com.netDashboard.icon.Icon
import com.netDashboard.icon.Icons.lineCats
import com.netDashboard.icon.Icons.lineIcons
import com.netDashboard.icon.Icons.solidCats
import com.netDashboard.icon.Icons.solidIcons
import com.netDashboard.icon.Icons.thinCats
import com.netDashboard.icon.Icons.thinIcons
import com.netDashboard.icon.IconsAdapter
import com.netDashboard.tile.Tile
import java.util.*

class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconsAdapter
    private lateinit var tile: Tile
    private var selectedChip = 0

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

        theme.apply(requireContext(), b.root, true)
        tile = dashboard.tiles[arguments?.getInt("index", 0) ?: 0]
        setupRecyclerView()


        b.tiLine.setOnClickListener {
            if(b.tiIconType.checkedChipId != 0) {
                updateRecyclerView(lineIcons, lineCats)
                adapter.notifyDataSetChanged()
            }
        }

        b.tiThin.setOnClickListener {
            if(b.tiIconType.checkedChipId != 1) {
                updateRecyclerView(thinIcons, thinCats)
                adapter.notifyDataSetChanged()
            }
        }

        b.tiSolid.setOnClickListener {
            if(b.tiIconType.checkedChipId != 2) {
                updateRecyclerView(solidIcons, solidCats)
                adapter.notifyDataSetChanged()
            }
        }
    }

    fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

    }

    private fun setupRecyclerView() {
        val spanCount = 6

        adapter = IconsAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemClick = { item ->
            if (!item.isCategory) {
                tile.icon = item
                (activity as MainActivity).fm.popBackStack()
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

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter
    }

    fun updateRecyclerView(icons: List<Icon>, cat: List<String>) {
        val list = mutableListOf<Icon>()
        adapter.submitList(list)

        for (c in cat) {
            val cUp =
                c.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            list.add(Icon(cat = cUp, isCategory = true))
            list.addAll(icons.filter { it.cat == c })
        }
    }
}