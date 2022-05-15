package com.alteratom.dashboard.activities.fragments.tile_properties

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconHSV
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.Theme.Companion.artist
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.Theme.Companion.isDark
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.TileIconFragment
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.FragmentTilePropertiesBinding
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
import java.util.*

class TilePropertiesFragment : Fragment(R.layout.fragment_tile_properties) {
    private lateinit var b: FragmentTilePropertiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().window.statusBarColor = artist.pallet.background
        WindowInsetsControllerCompat(requireActivity().window, requireActivity().window.decorView)
            .isAppearanceLightStatusBars = !isDark

        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colors.background
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //b.tpRoot.onInterceptTouch = { e ->
        //    TileSwitcher.handle(e)
        //}
    }
}

/*
Test ------------------------------------------------------------------------------------------

private object RippleCustomTheme : RippleTheme {

    @Composable
    override fun defaultColor() =
        RippleTheme.defaultRippleColor(
            Color(255, 255, 255),
            lightTheme = false
        )

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(
            Color(255, 255, 255),
            lightTheme = true
        )

Column(modifier = Modifier.padding(16.dp)) {
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Label") }
    )

    CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
        OutlinedButton(
            onClick = {},
            border = BorderStroke(0.dp, Color.White),
            shape = RectangleShape,
            modifier = Modifier.padding(top = 10.dp)
        ) {
            Text("TEST", color = Color.White)
        }
    }

    CustomView()
}

@Composable
fun CustomView() {
    val selectedItem = remember { mutableStateOf(0) }
    AndroidView(
        modifier = Modifier.fillMaxSize(), // Occupy the max size in the Compose UI tree
        factory = { context ->
            val view = LayoutInflater.from(context).inflate(R.layout.fragment_tile_new, null, false)
            view
        },
        update = { view ->
        }
    )
}
*/