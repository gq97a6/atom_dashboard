package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.ArcSlider
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.toPx

class ThemeFragment : Fragment() {

    @OptIn(ExperimentalGraphicsApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var hueAngle by remember { mutableStateOf(theme.a.hsv[0].toDouble()) }
                var saturationAngle by remember { mutableStateOf(100 + 160.0 * theme.a.hsv[1]) }
                var saturationDarkAngle by remember { mutableStateOf(110 + 320.0 * theme.a.hsv[1]) }
                var valueAngle by remember { mutableStateOf((440 - 160.0 * theme.a.hsv[2]) % 360) }

                var hue by remember { mutableStateOf(theme.a.hsv[0]) }
                var saturation by remember { mutableStateOf(theme.a.hsv[1]) }
                var value by remember { mutableStateOf(theme.a.hsv[2]) }

                var colors by remember { mutableStateOf(colors) }
                var isDark by remember { mutableStateOf(theme.a.isDark) }

                ComposeTheme(theme.a.isDark, theme.a.getComposeColorPallet()) {
                    theme.apply(context = requireContext())

                    //Background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.background)
                    )

                    Column {
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Dark background:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = isDark,
                            onCheckedChange = {
                                theme.a.isDark = it
                                isDark = it

                                if(it) {
                                    value = 1f
                                    valueAngle = 280.0
                                    theme.a.hsv = floatArrayOf(hue, saturation, value)
                                }

                                theme.apply((activity as MainActivity).b.root, requireContext())
                                colors = Theme.colors
                            },
                            colors = colors
                        )
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Dark background:",
                                    fontSize = 15.sp,
                                    color = colors.a
                                )
                            },
                            checked = false,
                            onCheckedChange = {
                            },
                            colors = colors
                        )
                    }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ArcSlider(
                            modifier = Modifier
                                .fillMaxSize(.8f)
                                .aspectRatio(1f),
                            angle = hueAngle,
                            startAngle = 0.0,
                            sweepAngle = 360.0,
                            strokeWidth = 15.dp.toPx(),
                            pointerRadius = 15.dp.toPx(),
                            pointerStyle = Stroke(width = 2.dp.toPx()),
                            pointerColor = Color.Gray,
                            colorList = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            ),
                            onChange = { a, v ->
                                hueAngle = a
                                hue = (v * 360f).toFloat()
                                theme.a.hsv = floatArrayOf(hue, saturation, value)
                                colors = Theme.colors
                            }
                        )

                        AnimatedVisibility(
                            visible = !isDark, enter = EnterTransition.None,
                            exit = ExitTransition.None
                        ) {
                            ArcSlider(
                                modifier = Modifier
                                    .fillMaxSize(.6f)
                                    .aspectRatio(1f),
                                angle = saturationAngle,
                                startAngle = 100.0,
                                sweepAngle = 160.0,
                                strokeWidth = 15.dp.toPx(),
                                pointerRadius = 15.dp.toPx(),
                                pointerStyle = Stroke(width = 1.dp.toPx()),
                                pointerColor = Color.Gray,
                                colorList = listOf(
                                    Color.hsv(hue, 1f, value),
                                    Color.hsv(hue, 0f, value),
                                ).asReversed(),
                                onChange = { a, v ->
                                    saturationAngle = a
                                    v.toFloat().let {
                                        saturationDarkAngle = 110 + 320.0 * it
                                        saturation = it
                                    }
                                    theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    colors = Theme.colors
                                }
                            )

                            ArcSlider(
                                modifier = Modifier
                                    .fillMaxSize(.6f)
                                    .aspectRatio(1f),
                                angle = valueAngle,
                                startAngle = 280.0,
                                sweepAngle = 160.0,
                                strokeWidth = 15.dp.toPx(),
                                pointerRadius = 15.dp.toPx(),
                                pointerStyle = Stroke(width = 1.dp.toPx()),
                                pointerColor = Color.Gray,
                                colorList = listOf(
                                    Color.hsv(hue, saturation, 1f),
                                    Color.hsv(hue, saturation, 0f)
                                ),
                                onChange = { a, v ->
                                    valueAngle = a
                                    value = (1 - v).toFloat()
                                    theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    colors = Theme.colors
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = isDark, enter = EnterTransition.None,
                            exit = ExitTransition.None
                        ) {
                            ArcSlider(
                                modifier = Modifier
                                    .fillMaxSize(.6f)
                                    .aspectRatio(1f),
                                angle = saturationDarkAngle,
                                startAngle = 110.0,
                                sweepAngle = 320.0,
                                strokeWidth = 15.dp.toPx(),
                                pointerRadius = 15.dp.toPx(),
                                pointerStyle = Stroke(width = 1.dp.toPx()),
                                pointerColor = Color.Gray,
                                colorList = listOf(
                                    Color.hsv(hue, 1f, value),
                                    Color.hsv(hue, 0f, value),
                                ).asReversed(),
                                onChange = { a, v ->
                                    saturationDarkAngle = a
                                    v.toFloat().let {
                                        saturationAngle = 100 + 160.0 * it
                                        saturation = it
                                    }
                                    theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    colors = Theme.colors
                                }
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize(.4f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    Color.hsv(
                                        theme.a.hsv[0],
                                        theme.a.hsv[1],
                                        theme.a.hsv[2]
                                    )
                                )
                        )
                    }
                }
            }
        }
    }
}