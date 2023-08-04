package com.alteratom.dashboard.activities.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.compose_global.ArcSlider
import com.alteratom.dashboard.compose_global.ComposeTheme
import com.alteratom.dashboard.compose_global.switchColors
import com.alteratom.dashboard.objects.G.areInitialized
import com.alteratom.dashboard.objects.G.dashboardIndex
import com.alteratom.dashboard.objects.G.hasBooted
import com.alteratom.dashboard.objects.G.hasShutdown
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.pHsv
import com.alteratom.dashboard.restart
import com.alteratom.dashboard.toPx

class ThemeFragment : Fragment() {

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
                var hueAngle by remember { mutableStateOf(theme.a.hsv[0].toDouble()) }
                var saturationAngle by remember { mutableStateOf(100 + 160.0 * theme.a.hsv[1]) }
                var saturationDarkAngle by remember { mutableStateOf(110 + 320.0 * theme.a.hsv[1]) }
                var valueAngle by remember { mutableStateOf((440 - 160.0 * theme.a.hsv[2]) % 360) }

                var hue by remember { mutableStateOf(theme.a.hsv[0]) }
                var saturation by remember { mutableStateOf(theme.a.hsv[1]) }
                var value by remember { mutableStateOf(theme.a.hsv[2]) }

                var isDark by remember { mutableStateOf(theme.a.isDark) }

                ComposeTheme(theme.a.isDark) {
                    theme.apply(context = requireContext())

                    Column(
                        horizontalAlignment = CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            ArcSlider(
                                modifier = Modifier
                                    .fillMaxSize(.8f),
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
                                }
                            )

                            if (isDark) {
                                ArcSlider(
                                    modifier = Modifier.fillMaxSize(.6f),
                                    angle = saturationDarkAngle,
                                    startAngle = 110.0,
                                    sweepAngle = 320.0,
                                    strokeWidth = 15.dp.toPx(),
                                    pointerRadius = 15.dp.toPx(),
                                    pointerStyle = Stroke(width = 1.dp.toPx()),
                                    pointerColor = Color.Gray,
                                    colorList = listOf(
                                        pHsv(hue, 1f, 1f),
                                        pHsv(hue, 0f, 1f),
                                    ).asReversed(),
                                    onChange = { a, v ->
                                        saturationDarkAngle = a
                                        v.toFloat().let {
                                            saturationAngle = 100 + 160.0 * it
                                            saturation = it
                                        }
                                        theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    }
                                )
                            } else {
                                ArcSlider(
                                    modifier = Modifier.fillMaxSize(.6f),
                                    angle = saturationAngle,
                                    startAngle = 100.0,
                                    sweepAngle = 160.0,
                                    strokeWidth = 15.dp.toPx(),
                                    pointerRadius = 15.dp.toPx(),
                                    pointerStyle = Stroke(width = 1.dp.toPx()),
                                    pointerColor = Color.Gray,
                                    colorList = listOf(
                                        pHsv(hue, 1f, value),
                                        pHsv(hue, 0f, value),
                                    ).asReversed(),
                                    onChange = { a, v ->
                                        saturationAngle = a
                                        v.toFloat().let {
                                            saturationDarkAngle = 110 + 320.0 * it
                                            saturation = it
                                        }
                                        theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    }
                                )

                                ArcSlider(
                                    modifier = Modifier.fillMaxSize(.6f),
                                    angle = valueAngle,
                                    startAngle = 280.0,
                                    sweepAngle = 160.0,
                                    strokeWidth = 15.dp.toPx(),
                                    pointerRadius = 15.dp.toPx(),
                                    pointerStyle = Stroke(width = 1.dp.toPx()),
                                    pointerColor = Color.Gray,
                                    colorList = listOf(
                                        pHsv(hue, saturation, 1f),
                                        pHsv(hue, saturation, 0f)
                                    ),
                                    onChange = { a, v ->
                                        valueAngle = a
                                        value = (1 - v).toFloat()
                                        theme.a.hsv = floatArrayOf(hue, saturation, value)
                                    }
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize(.4f)
                                    .clip(CircleShape)
                                    .background(
                                        pHsv(
                                            hue,
                                            saturation,
                                            if (isDark) 1f else value
                                        )
                                    )
                            )
                        }

                        Text(
                            "DARK BACKGROUND",
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            color = colors.b
                        )

                        Row(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(bottom = 100.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "OFF",
                                fontSize = 30.sp,
                                color = colors.b
                            )
                            Switch(
                                modifier = Modifier
                                    .scale(2f)
                                    .padding(20.dp),
                                checked = isDark,
                                colors = switchColors(),
                                onCheckedChange = {
                                    theme.a.isDark = it
                                    isDark = it

                                    theme.apply((activity as MainActivity).b.root, requireContext())
                                }
                            )

                            Text("ON", fontSize = 30.sp, color = colors.b)
                        }
                    }
                }
            }
        }
    }
}