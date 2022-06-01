package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.GridItemSpan
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import com.alteratom.R
import com.alteratom.dashboard.ArcSlider
import com.alteratom.dashboard.G.getIconColorPallet
import com.alteratom.dashboard.G.getIconHSV
import com.alteratom.dashboard.G.getIconRes
import com.alteratom.dashboard.G.setIconHSV
import com.alteratom.dashboard.G.setIconKey
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.dialogConfirmCompose
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.toPx
import java.util.*


class TileIconFragment : Fragment(R.layout.fragment_tile_icon) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())
    }

    val cols = mutableListOf<Pair<Color, FloatArray>>().apply {
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

    val icons = Icons.cats.map { c ->
        val catUp = c.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }
        catUp to Icons.icons.filter { it.value.cat == c }.values.toList()
    }.toMap()

    @OptIn(ExperimentalFoundationApi::class, ExperimentalGraphicsApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                var iconRes by remember { mutableStateOf(getIconRes()) }
                var iconColor by remember { mutableStateOf(getIconColorPallet().cc) }

                var hueAngle by remember { mutableStateOf(getIconHSV()[0].toDouble()) }
                var saturationAngle by remember { mutableStateOf(100 + 160.0 * getIconHSV()[1]) }
                var saturationDarkAngle by remember { mutableStateOf(110 + 320.0 * getIconHSV()[1]) }
                var valueAngle by remember { mutableStateOf((440 - 160.0 * getIconHSV()[2]) % 360) }

                var hue by remember { mutableStateOf(theme.a.hsv[0]) }
                var saturation by remember { mutableStateOf(theme.a.hsv[1]) }
                var value by remember { mutableStateOf(theme.a.hsv[2]) }

                ComposeTheme(Theme.isDark) {

                    var showPicker by remember {mutableStateOf(false)}

                    //Background
                    Box(modifier = Modifier.background(colors.background))

                    Box {

                        LazyVerticalGrid(
                            cells = GridCells.Fixed(6),
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            item({ GridItemSpan(6) }) {
                                Spacer(modifier = Modifier.height(140.dp))
                            }

                            items(cols, { GridItemSpan(1) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(it.first)
                                        .clickable {
                                            setIconHSV(it.second)
                                            iconColor = getIconColorPallet().cc
                                        }
                                )
                            }

                            item({ GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (theme.a.isDark) Color.White else Color.Black)
                                        .clickable {
                                            setIconHSV(floatArrayOf(0f, 0f, 0f))
                                            iconColor = getIconColorPallet().cc
                                        }
                                )
                            }

                            item({ GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .height(45.dp)
                                        .border(
                                            BorderStroke(0.dp, colors.color),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .clickable {
                                            showPicker = true
                                        }
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
                                item({ GridItemSpan(6) }) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 15.dp)
                                            .padding(bottom = 5.dp)
                                            .padding(horizontal = 8.dp)
                                            .height(50.dp)
                                            .border(
                                                BorderStroke(0.dp, colors.color),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(start = 10.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(pair.key, fontSize = 20.sp, color = colors.a)
                                    }
                                }

                                items(pair.value, { GridItemSpan(1) }) {
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

                                item({ GridItemSpan(6) }) {
                                    Spacer(Modifier)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .size(100.dp)
                                .border(
                                    BorderStroke(0.dp, iconColor.a),
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
                                tint = iconColor.a,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 7.dp)
                            )
                        }

                        if(showPicker) {
                            Dialog(onDismissRequest = { showPicker = false }) {

                                Surface(Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f),
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
                                                iconColor = getIconColorPallet().cc
                                            }
                                        )

                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = !theme.a.isDark, enter = EnterTransition.None,
                                            exit = ExitTransition.None
                                        ) {
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
                                                    v.toFloat().let {
                                                        saturationDarkAngle = 110 + 320.0 * it
                                                        saturation = it
                                                    }
                                                    setIconHSV(floatArrayOf(hue, saturation, value))
                                                    iconColor = getIconColorPallet().cc
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
                                                    iconColor = getIconColorPallet().cc
                                                }
                                            )
                                        }

                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = theme.a.isDark, enter = EnterTransition.None,
                                            exit = ExitTransition.None
                                        ) {
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
                                                    Color.hsv(hue, 1f, value),
                                                    Color.hsv(hue, 0f, value),
                                                ).asReversed(),
                                                onChange = { a, v ->
                                                    saturationDarkAngle = a
                                                    v.toFloat().let {
                                                        saturationAngle = 100 + 160.0 * it
                                                        saturation = it
                                                    }
                                                    setIconHSV(floatArrayOf(hue, saturation, value))
                                                    iconColor = getIconColorPallet().cc
                                                }
                                            )
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(.4f)
                                                .clip(CircleShape)
                                                .background(iconColor.color)
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
}