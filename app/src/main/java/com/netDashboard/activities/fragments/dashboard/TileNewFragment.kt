package com.netDashboard.activities.fragments.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.activities.fragments.dashboard.tile_properties.TilePropertiesFragment
import com.netDashboard.app_on.AppOn
import com.netDashboard.click
import com.netDashboard.databinding.FragmentTileNewBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.slider.SliderTile

class TileNewFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentTileNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTileNewBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AppOn.create(requireActivity())

        G.theme.apply(requireActivity(), b.root)

        b.tnButton.setOnClickListener {
            b.tnButtonRipple.click()
            addTile(ButtonTile())
        }

        b.tnSlider.setOnClickListener {
            b.tnSliderRipple.click()
            addTile(SliderTile())
        }

        b.tnText.setOnClickListener {
            b.tnTextRipple.click()
            addTile(TextTile())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }

    private var isDone = false
    private fun addTile(tile: Tile) {
        if (isDone) return
        isDone = true

        tile.dashboard = dashboard
        dashboard.tiles.add(tile)

        parentFragmentManager.beginTransaction().apply {
            replace(R.id.m_fragment, TilePropertiesFragment())
            commit()
        }
    }
}