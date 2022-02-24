package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.click
import com.netDashboard.databinding.FragmentTileNewBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.tile
import com.netDashboard.tile.Tile
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.button.TextTile
import com.netDashboard.tile.types.pick.SelectTile
import com.netDashboard.tile.types.slider.SliderTile
import com.netDashboard.tile.types.switch.SwitchTile
import com.netDashboard.tile.types.terminal.TerminalTile
import com.netDashboard.tile.types.time.TimeTile

class TileNewFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentTileNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentTileNewBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        G.theme.apply(b.root, requireActivity())

        b.tnButton.setOnClickListener {
            b.tnButtonRipple.click()
            addTile(ButtonTile())
        }

        b.tnSlider.setOnClickListener {
            b.tnSliderRipple.click()
            addTile(SliderTile())
        }

        b.tnSwitch.setOnClickListener {
            b.tnSwitchRipple.click()
            addTile(SwitchTile())
        }

        b.tnText.setOnClickListener {
            b.tnTextRipple.click()
            addTile(TextTile())
        }

        b.tnSelect.setOnClickListener {
            b.tnSelectRipple.click()
            addTile(SelectTile())
        }

        b.tnTerminal.setOnClickListener {
            b.tnTerminalRipple.click()
            addTile(TerminalTile())
        }

        b.tnTime.setOnClickListener {
            b.tnTimeRipple.click()
            addTile(TimeTile())
        }
//
        //b.tnColor.setOnClickListener {
        //    b.tnColorRipple.click()
        //    addTile(ColorTile())
        //}
//
        //b.tnThermostat.setOnClickListener {
        //    b.tnThermostatRipple.click()
        //    addTile(ThermostatTile())
        //}
//
        //b.tnLights.setOnClickListener {
        //    b.tnLightsRipple.click()
        //    addTile(LightsTile())
        //}
    }

    private var isDone = false
    private fun addTile(t: Tile) {
        if (isDone) return
        isDone = true

        t.dashboard = dashboard
        t.onCreateTile()
        dashboard.tiles.add(t)

        tile = t
        (activity as MainActivity).fm.replaceWith(TilePropertiesFragment(), false)
    }
}