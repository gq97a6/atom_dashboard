package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.click
import com.alteratom.databinding.FragmentTileNewBinding
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.tile.Tile
import com.alteratom.tile.types.button.ButtonTile
import com.alteratom.tile.types.button.TextTile
import com.alteratom.tile.types.color.ColorTile
import com.alteratom.tile.types.lights.LightsTile
import com.alteratom.tile.types.pick.SelectTile
import com.alteratom.tile.types.slider.SliderTile
import com.alteratom.tile.types.switch.SwitchTile
import com.alteratom.tile.types.terminal.TerminalTile
import com.alteratom.tile.types.thermostat.ThermostatTile
import com.alteratom.tile.types.time.TimeTile

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

        b.tnColor.setOnClickListener {
            b.tnColorRipple.click()
            addTile(ColorTile())
        }

        b.tnThermostat.setOnClickListener {
            b.tnThermostatRipple.click()
            addTile(ThermostatTile())
        }

        b.tnLights.setOnClickListener {
            b.tnLightsRipple.click()
            addTile(LightsTile())
        }
    }

    private var isDone = false
    private fun addTile(t: Tile) {
        if (isDone) return
        isDone = true

        t.dashboard = dashboard
        t.onCreateTile()
        dashboard.tiles.add(t)

        tile = t
        fm.replaceWith(TilePropertiesFragment(), false)
    }
}