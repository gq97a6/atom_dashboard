package com.alteratom.dashboard.activities.fragments.tile_properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.tile.types.button.ButtonTile
import com.alteratom.tile.types.button.TextTile
import com.alteratom.tile.types.button.compose.ButtonTileCompose
import com.alteratom.tile.types.color.ColorTile
import com.alteratom.tile.types.color.compose.*
import com.alteratom.tile.types.lights.LightsTile
import com.alteratom.tile.types.pick.SelectTile
import com.alteratom.tile.types.slider.SliderTile
import com.alteratom.tile.types.switch.SwitchTile
import com.alteratom.tile.types.terminal.TerminalTile
import com.alteratom.tile.types.thermostat.ThermostatTile
import com.alteratom.tile.types.time.TimeTile

class TilePropertiesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theme.apply(context = requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                //Background
                Box(modifier = Modifier.background(Theme.colors.background))

                ComposeTheme(isDark) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        TileSwitcher.handle(awaitPointerEvent())
                                    }
                                }
                            }
                    ) {
                        tile.dashboard?.type?.let {
                            when (tile) {
                                is ButtonTile -> ButtonTileCompose
                                is ColorTile -> ColorTileCompose
                                is LightsTile -> LightsTileCompose
                                is SelectTile -> SelectTileCompose
                                is SliderTile -> SliderTileCompose
                                is SwitchTile -> SwitchTileCompose
                                is TerminalTile -> TerminalTileCompose
                                is TextTile -> TextTileCompose
                                is ThermostatTile -> ThermostatTileCompose
                                is TimeTile -> TimeTileCompose
                                else -> ButtonTileCompose
                            }.compose(it)
                        }
                    }
                }
            }
        }
    }
}