package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.netDashboard.R
import com.netDashboard.databinding.FragmentTileIconBinding
import com.netDashboard.globals.G.dashboard
import com.netDashboard.icon.Icons.lineIcons
import com.netDashboard.icon.IconsAdapter
import com.netDashboard.switchTo
import com.netDashboard.tile.Tile

class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {
    private lateinit var b: FragmentTileIconBinding

    private lateinit var adapter: IconsAdapter
    private lateinit var tile: Tile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTileIconBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tile = dashboard.tiles[arguments?.getInt("index", 0) ?: 0]
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val spanCount = 6

        adapter = IconsAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemClick = { item ->
            tile.icon = item
            parentFragmentManager.popBackStack()
        }

        adapter.submitList(lineIcons.toMutableList())

        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter
    }
}