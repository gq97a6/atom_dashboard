package com.alteratom.dashboard.activities.fragments

import ButtonTile
import ButtonTileCompose
import ColorTile
import ColorTileCompose
import LightsTile
import LightsTileCompose
import SelectTile
import SelectTileCompose
import SliderTile
import SliderTileCompose
import SwitchTile
import SwitchTileCompose
import TerminalTile
import TerminalTileCompose
import TextTile
import TextTileCompose
import ThermostatTile
import ThermostatTileCompose
import TimeTile
import TimeTileCompose
import android.content.Context
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
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.compose_global.ComposeTheme
import com.alteratom.dashboard.objects.G.areInitialized
import com.alteratom.dashboard.objects.G.dashboardIndex
import com.alteratom.dashboard.objects.G.hasBooted
import com.alteratom.dashboard.objects.G.hasShutdown
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.restart
import com.alteratom.dashboard.switcher.TileSwitcher

class TilePropertiesFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (dashboardIndex < 0 || !areInitialized || !hasBooted || hasShutdown) requireActivity().restart()
    }

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
                            }.Compose(it, this@TilePropertiesFragment)
                        }
                    }
                }
            }
        }
    }
}