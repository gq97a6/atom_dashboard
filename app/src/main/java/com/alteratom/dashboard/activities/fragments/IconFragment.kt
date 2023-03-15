package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ArcSlider
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.toPx
import java.util.*

class TileIconFragment : Fragment() {

    companion object {
        lateinit var setIconHSV: (FloatArray) -> Unit
        lateinit var setIconKey: (String) -> Unit
        lateinit var getIconRes: () -> Int
        lateinit var getIconHSV: () -> FloatArray
        lateinit var getIconColorPallet: () -> Theme.ColorPallet
    }

    private val cols = mutableListOf<Pair<Color, FloatArray>>().apply {
        for (i in 40..100 step 20) {
            for (ii in 0..300 step 60) {
                val hsv = if (theme.a.isDark) floatArrayOf(
                    ii.toFloat(),
                    i.toFloat() / 100,
                    1f
                ) else floatArrayOf(ii.toFloat(), 1f, i.toFloat() / 100)

                add(theme.a.getColorPallet(hsv, true).cc.color to hsv)
            }
        }
    }.toList()

    private val icons = Icons.cats.associate { c ->
        val catUp = c.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        catUp to Icons.icons.filter { it.value.cat == c }.values.toList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theme.apply(context = requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                var iconRes by remember { mutableStateOf(getIconRes()) }
                var iconColor by remember { mutableStateOf(getIconColorPallet().cc.color) }
                var pickerShow by remember { mutableStateOf(false) }

                var hueAngle by remember { mutableStateOf(0.0) }
                var saturationAngle by remember { mutableStateOf(0.0) }
                var valueAngle by remember { mutableStateOf(0.0) }

                var hue by remember { mutableStateOf(0f) }
                var saturation by remember { mutableStateOf(0f) }
                var value by remember { mutableStateOf(0f) }

                fun setPickerColor(hsv: FloatArray) {
                    hue = hsv[0]
                    saturation = hsv[1]
                    value = hsv[2]

                    hueAngle = hsv[0].toDouble()
                    saturationAngle = if (theme.a.isDark) 110 + 320.0 * hsv[1]
                    else 100 + 160.0 * hsv[1]
                    valueAngle = (440 - 160.0 * hsv[2]) % 360
                }

                setPickerColor(getIconHSV())

                ComposeTheme(Theme.isDark) {

                    Box {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(6),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            item(span = { GridItemSpan(6) }) {
                                Spacer(modifier = Modifier.height(140.dp))
                            }

                            items(cols, span = { GridItemSpan(1) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(it.first)
                                        .clickable {
                                            setPickerColor(it.second)
                                            setIconHSV(it.second)
                                            iconColor = it.first
                                        }
                                )
                            }

                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (theme.a.isDark) Color.White else Color.Black)
                                        .clickable {
                                            setPickerColor(floatArrayOf(0f, 0f, 0f))
                                            setIconHSV(floatArrayOf(0f, 0f, 0f))
                                            iconColor = getIconColorPallet().cc.color
                                        }
                                )
                            }

                            item(span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .clickable {
                                            pickerShow = true
                                        }
                                        .border(
                                            BorderStroke(1.dp, colors.color),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painterResource(R.drawable.il_interface_question),
                                        "",
                                        tint = colors.a,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(1f)
                                    )
                                }
                            }

                            for (pair in icons) {
                                item(span = { GridItemSpan(6) }) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 15.dp)
                                            .padding(bottom = 5.dp)
                                            .padding(horizontal = 8.dp)
                                            .height(50.dp)
                                            .border(
                                                BorderStroke(1.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(start = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(pair.key, fontSize = 20.sp, color = colors.a)
                                    }
                                }

                                items(pair.value, span = { GridItemSpan(1) }) {
                                    Icon(
                                        painterResource(it.res),
                                        "",
                                        tint = colors.a,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .aspectRatio(1f)
                                            .clickable {
                                                val key =
                                                    Icons.icons.filterValues { i -> i.res == it.res }.keys
                                                setIconKey(key.first())
                                                iconRes = getIconRes()
                                            }
                                    )
                                }

                                item(span = { GridItemSpan(6) }) {
                                    Spacer(Modifier)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(100.dp)
                                .border(
                                    BorderStroke(1.dp, iconColor),
                                    RoundedCornerShape(10.dp)
                                )
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.background.copy(.8f))
                                .padding(10.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Icon(
                                painterResource(iconRes),
                                "",
                                tint = iconColor,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (pickerShow) {
                            Dialog({ pickerShow = false }) {
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
                                            setIconHSV(floatArrayOf(hue, saturation, value))
                                            iconColor = getIconColorPallet().cc.color
                                        }
                                    )

                                    if (theme.a.isDark) {
                                        ArcSlider(
                                            modifier = Modifier.fillMaxSize(.6f),
                                            angle = saturationAngle,
                                            startAngle = 110.0,
                                            sweepAngle = 320.0,
                                            strokeWidth = 15.dp.toPx(),
                                            pointerRadius = 15.dp.toPx(),
                                            pointerStyle = Stroke(width = 1.dp.toPx()),
                                            pointerColor = Color.Gray,
                                            colorList = listOf(
                                                Color.hsv(hue, 1f, 1f),
                                                Color.hsv(hue, 0f, 1f),
                                            ).asReversed(),
                                            onChange = { a, v ->
                                                saturationAngle = a
                                                saturation = v.toFloat()
                                                setIconHSV(floatArrayOf(hue, saturation, value))
                                                iconColor = getIconColorPallet().cc.color
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
                                                Color.hsv(hue, 1f, value),
                                                Color.hsv(hue, 0f, value),
                                            ).asReversed(),
                                            onChange = { a, v ->
                                                saturationAngle = a
                                                saturation = v.toFloat()
                                                setIconHSV(floatArrayOf(hue, saturation, value))
                                                iconColor = getIconColorPallet().cc.color
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
                                                Color.hsv(hue, saturation, 1f),
                                                Color.hsv(hue, saturation, 0f)
                                            ),
                                            onChange = { a, v ->
                                                valueAngle = a
                                                value = (1 - v).toFloat()
                                                setIconHSV(floatArrayOf(hue, saturation, value))
                                                iconColor = getIconColorPallet().cc.color
                                            }
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(.4f)
                                            .clip(CircleShape)
                                            .background(
                                                Color.hsv(
                                                    hue,
                                                    saturation,
                                                    if (theme.a.isDark) 1f else value
                                                )
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}