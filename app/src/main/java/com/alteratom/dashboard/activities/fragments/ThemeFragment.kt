package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.alteratom.dashboard.ArcSlider
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.LabeledSwitch
import com.alteratom.dashboard.Theme
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
                ComposeTheme(Theme.isDark) {
                    var hueAngle by remember { mutableStateOf(theme.a.hsv[0].toDouble()) }
                    var saturationAngle by remember { mutableStateOf(100 + 160.0 * theme.a.hsv[1]) }
                    var valueAngle by remember { mutableStateOf((440 - 160.0 * theme.a.hsv[2]) % 360) }

                    var color by remember {
                        mutableStateOf(
                            Color.hsv(
                                theme.a.hsv[0],
                                theme.a.hsv[1],
                                theme.a.hsv[2]
                            )
                        )
                    }
                    var hue by remember { mutableStateOf(theme.a.hsv[0]) }
                    var saturation by remember { mutableStateOf(theme.a.hsv[1]) }
                    var value by remember { mutableStateOf(theme.a.hsv[2]) }

                    fun onColorChanged() {
                        color = Color.hsv(hue, saturation, value)
                        theme.a.hsv = floatArrayOf(hue, saturation, value)
                    }

                    var isDark by remember { mutableStateOf(theme.a.isDark) }
                    Column {
                        LabeledSwitch(
                            label = {
                                Text(
                                    "Dark background:",
                                    fontSize = 15.sp,
                                    color = Theme.colors.a
                                )
                            },
                            checked = isDark,
                            onCheckedChange = {
                                isDark = it
                                theme.a.isDark = it
                            },
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
                                theme.a.hsv[0] = a.toFloat()
                                onColorChanged()
                            }
                        )

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
                                saturation = v.toFloat()
                                theme.a.hsv[1] = v.toFloat()
                                onColorChanged()
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
                                theme.a.hsv[2] = v.toFloat()
                                onColorChanged()
                            }
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize(.4f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }
            }
        }
    }

    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewConfig()
        theme.apply(b.root, requireContext())

        fun onColorChange() {
            theme.a.hsv = floatArrayOf(b.tHue.value, b.tSaturation.value, b.tValue.value)

            theme.apply((activity as MainActivity).b.root, requireContext())
        }

        b.tHue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tSaturation.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tValue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == KeyEvent.ACTION_UP
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.a.isDark = state

            b.tValText.tag = if (state) "colorC" else "colorB"
            b.tValue.tag = if (state) "disabled" else "enabled"
            b.tValue.isEnabled = !state
            if (state) b.tValue.value = 1f
            else b.tValue.value = theme.a.hsv[2]

            theme.apply((activity as MainActivity).b.root, requireContext())
        }

        b.tAdvancedArrow.setOnClickListener {
            switchAdvancedTab()
        }
    }

    private fun viewConfig() {
        b.tHue.value = theme.a.hsv[0]
        b.tSaturation.value = theme.a.hsv[1]
        b.tValue.value = theme.a.hsv[2]

        b.tValText.tag = if (theme.a.isDark) "colorC" else "colorB"
        b.tValue.tag = if (theme.a.isDark) "disabled" else "enabled"
        b.tValue.isEnabled = !theme.a.isDark
        if (theme.a.isDark) b.tValue.value = 1f

        if (b.tSaturation.value + b.tValue.value < 2) {
            b.tAdvancedArrow.rotation = 0f
            b.tAdvanced.visibility = View.VISIBLE
        }

        b.tIsDark.isChecked = theme.a.isDark

        b.tBar.post {
            b.tBar.translationY = -1.5f * b.tBar[0].height.toFloat()
        }
    }

    private fun switchAdvancedTab() {
        b.tAdvanced.let {
            it.visibility = if (it.isVisible) View.GONE else View.VISIBLE
            b.tAdvancedArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
    */
}