package com.alteratom.dashboard.fragment

import ButtonTile
import ColorTile
import LightsTile
import SelectTile
import SliderTile
import SwitchTile
import TerminalTile
import TextTile
import ThermostatTile
import TimeTile
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activity.MainActivity.Companion.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.compose_global.composeConstruct

class TileNewFragment : Fragment() {

    val tileTags = listOf(
        "Button",
        "Slider",
        "Switch",
        "Text",
        "Select",
        "Terminal",
        "Color picker",
        "Time",
        "Lights",
        "Thermostat"
    )

    val tileIcons = listOf(
        R.drawable.il_arrow_arrow_to_bottom,
        R.drawable.il_arrow_arrows_h_alt,
        R.drawable.il_interface_toggle_on,
        R.drawable.il_design_illustration,
        R.drawable.il_business_receipt_alt,
        R.drawable.il_device_desktop,
        R.drawable.il_design_palette,
        R.drawable.il_time_clock,
        R.drawable.il_business_lightbulb_alt,
        R.drawable.il_weather_temperature_half
    )

    val tiles = listOf(
        { ButtonTile() },
        { SliderTile() },
        { SwitchTile() },
        { TextTile() },
        { SelectTile() },
        { TerminalTile() },
        { ColorTile() },
        { TimeTile() },
        { LightsTile() },
        { ThermostatTile() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeConstruct(requireContext()) {

        @Composable
        fun TilePick(index: Int, aspectRatio: Float) {
            Column(
                modifier = Modifier
                    .aspectRatio(aspectRatio)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        val tile = tiles[index]()
                        tile.dashboard = aps.dashboard
                        tile.onCreateTile()
                        aps.dashboard.tiles.add(tile)

                        aps.tile = tile
                        fm.replaceWith(TilePropertiesFragment(), false)
                    }
                    .border(
                        BorderStroke(1.dp, colors.color),
                        RoundedCornerShape(10.dp)
                    ),
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painterResource(tileIcons[index]),
                    "",
                    tint = colors.a,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    tileTags[index],
                    fontSize = 15.sp,
                    color = colors.b
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = CenterHorizontally
        ) {
            Text(
                "Pick new tile",
                fontSize = 40.sp,
                color = colors.a,
                modifier = Modifier.fillMaxWidth(.85f)
            )

            Text(
                "You will be redirected to its\nproperties afterwards",
                fontSize = 15.sp,
                color = colors.b,
                modifier = Modifier.fillMaxWidth(.85f)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(.85f)
            ) {
                items(10, span = { index ->
                    GridItemSpan(if (index in listOf(6, 9)) 2 else 1)
                }) { index ->
                    TilePick(
                        index, if (index in listOf(
                                6, 9
                            )
                        ) 2f else 1f
                    )
                }
            }

            Spacer(modifier = Modifier.fillMaxHeight(.2f))
        }
    }
}
