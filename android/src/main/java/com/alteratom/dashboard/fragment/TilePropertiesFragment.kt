package com.alteratom.dashboard.fragment

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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.compose_global.BasicButton
import com.alteratom.dashboard.compose_global.BoldStartText
import com.alteratom.dashboard.compose_global.EditText
import com.alteratom.dashboard.compose_global.NavigationArrows
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.G
import com.alteratom.dashboard.helper_objects.G.tile
import com.alteratom.dashboard.switcher.TileSwitcher
import java.util.Locale

class TilePropertiesFragment : Fragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)

        //Set gesture reaction
        MainActivity.onGlobalTouch = { e ->
            TileSwitcher.handle(e)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeConstruct(requireContext()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                //.pointerInput(Unit) {
                //    awaitPointerEventScope {
                //        while (true) {
                //            TileSwitcher.handle(awaitPointerEvent())
                //        }
                //    }
                //}
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Tile properties", fontSize = 45.sp, color = Theme.colors.color)
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                BasicButton(
                    contentPadding = PaddingValues(13.dp),
                    onClick = {
                        TileIconFragment.getIconHSV = { tile.hsv }
                        TileIconFragment.getIconRes = { tile.iconRes }
                        TileIconFragment.getIconColorPallet = { tile.pallet }

                        TileIconFragment.setIconHSV = { hsv -> tile.hsv = hsv }
                        TileIconFragment.setIconKey = { key -> tile.iconKey = key }

                        fm.replaceWith(TileIconFragment())
                    },
                    border = BorderStroke(1.dp, tile.pallet.cc.color),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(painterResource(tile.iconRes), "", tint = tile.pallet.cc.color)
                }

                val typeTag = tile.typeTag.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                }

                var tag by remember { mutableStateOf(tile.tag) }
                EditText(
                    label = { BoldStartText("$typeTag ", "tile tag") },
                    value = tag,
                    onValueChange = {
                        tag = it
                        tile.tag = it
                    },
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

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

            if (!G.settings.hideNav && G.dashboards.size > 1) Spacer(modifier = Modifier.height(60.dp))
        }

        if (!G.settings.hideNav && G.dashboard.tiles.size > 1) NavigationArrows(
            { TileSwitcher.switch(false) },
            { TileSwitcher.switch(true) })
    }
}
