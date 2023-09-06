package com.alteratom.dashboard.activity.fragments

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose_global.composeConstruct
import com.alteratom.dashboard.`object`.FragmentManager.fm
import com.alteratom.dashboard.`object`.G
import com.alteratom.dashboard.`object`.G.dashboard
import com.alteratom.dashboard.tile.Tile

class TileNewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = composeConstruct(requireContext()) {
        Column(
            Modifier.padding(bottom = 20.dp),
            Arrangement.Center,
            CenterHorizontally
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

            @Composable
            fun Frame(
                modifier: Modifier,
                type: String,
                res: Int,
                tile: Tile?
            ) {
                Column(
                    modifier = modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(tile != null) {
                            tile?.let {
                                it.dashboard = dashboard
                                it.onCreateTile()
                                dashboard.tiles.add(it)

                                G.tile = it
                                fm.replaceWith(TilePropertiesFragment(), false)
                            }
                        }
                        .border(
                            BorderStroke(1.dp, colors.color),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(10.dp)
                        .padding(bottom = 5.dp),
                    horizontalAlignment = CenterHorizontally
                ) {
                    Text(
                        type,
                        fontSize = 15.sp,
                        color = colors.b
                    )

                    Icon(
                        painterResource(res),
                        "",
                        tint = colors.a,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .padding(top = 7.dp)
                    )
                }
            }

            val padding = 10.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth(.85f)
                    .padding(top = 20.dp)
                    .border(
                        BorderStroke(1.dp, colors.color),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(end = padding, bottom = padding, top = padding)
            ) {
                Row {
                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Button",
                        R.drawable.il_arrow_arrow_to_bottom,
                        ButtonTile()
                    )

                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Slider",
                        R.drawable.il_arrow_arrows_h_alt,
                        SliderTile()
                    )

                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Switch",
                        R.drawable.il_interface_toggle_on,
                        SwitchTile()
                    )
                }

                Row(modifier = Modifier.padding(top = padding)) {
                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Text",
                        R.drawable.il_design_illustration,
                        TextTile()
                    )

                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Select",
                        R.drawable.il_business_receipt_alt,
                        SelectTile()
                    )
                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Terminal",
                        R.drawable.il_device_desktop,
                        TerminalTile()
                    )
                }

                Row(modifier = Modifier.padding(top = padding)) {
                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1.5f),
                        "Color picker",
                        R.drawable.il_design_palette,
                        ColorTile()
                    )

                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1.5f),
                        "Thermostat",
                        R.drawable.il_weather_temperature_half,
                        ThermostatTile()
                    )
                }

                Row(modifier = Modifier.padding(top = padding)) {
                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Time",
                        R.drawable.il_time_clock,
                        TimeTile()
                    )

                    Frame(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        "Lights",
                        R.drawable.il_business_lightbulb_alt,
                        LightsTile()
                    )

                    Box(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Center
                    ) {
                        Text(
                            "COMING\nSOON",
                            modifier = Modifier.rotate(10f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = colors.b
                        )
                        Frame(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(.3f),
                            "Graph",
                            R.drawable.il_business_analysis,
                            null
                        )
                    }
                }

                Row(modifier = Modifier.padding(top = padding)) {
                    Box(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1.5f),
                        contentAlignment = Center
                    ) {
                        Text(
                            "COMING\nSOON",
                            modifier = Modifier.rotate(350f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = colors.b
                        )

                        Frame(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(.3f),
                            "Keyboard",
                            R.drawable.il_interface_keyboard,
                            null
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(start = padding)
                            .weight(1f)
                            .aspectRatio(1.5f),
                        contentAlignment = Center
                    ) {
                        Text(
                            "COMING\nSOON",
                            modifier = Modifier.rotate(20f),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = colors.b
                        )

                        Frame(
                            modifier = Modifier
                                .fillMaxSize()
                                .alpha(.3f),
                            "Touchpad",
                            R.drawable.il_device_mouse_alt,
                            null
                        )
                    }
                }
            }
        }
    }
}
