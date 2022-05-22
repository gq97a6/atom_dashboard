package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.toDp
import com.alteratom.dashboard.toPx
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class SplashScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theme.apply(context = requireContext())

        return ComposeView(requireContext()).apply {
            setContent {

                ComposeTheme(Theme.isDark) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        RoundSlider(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(300.dp),
                            colorList = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                            //brush = Brush.sweepGradient(
                            //    0.0f to Color.Red,
                            //    0.5f / 6f * 1f to Color.Yellow,
                            //    0.5f / 6f * 2f to Color.Green,
                            //    0.5f / 6f * 3f to Color.Cyan,
                            //    0.5f / 6f * 4f to Color.Blue,
                            //    0.5f / 6f * 5f to Color.Magenta,
                            //    0.5f / 6f * 6f to Color.Red
                            //)
                        )
                    }
                }
            }
/*
            setContent {
                var serviceReady by remember { mutableStateOf(false) }

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = Theme.colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
                            val icon = if (Theme.isDark) R.drawable.ic_icon_light
                            else R.drawable.ic_icon

                            val rotation = if (serviceReady) 200f else 0f
                            val rotate: Float by animateFloatAsState(
                                targetValue = rotation,
                                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                            )

                            val size = if (serviceReady) 2f else 1f
                            val scale: Float by animateFloatAsState(
                                targetValue = size,
                                animationSpec = tween(durationMillis = 600, easing = LinearEasing)
                            )

                            Image(
                                painterResource(icon), "Logo",
                                modifier = Modifier
                                    .size(300.dp)
                                    .scale(scale)
                                    .rotate(rotate),
                                colorFilter = ColorFilter.tint(
                                    Theme.colors.color.copy(alpha = .4f),
                                    BlendMode.SrcAtop
                                )
                            )

                            Spacer(modifier = Modifier.fillMaxHeight(.2f))
                        }
                    }
                }

                if (serviceReady) {
                    if (settings.startFromLast && G.setCurrentDashboard(settings.lastDashboardId))
                        fm.replaceWith(DashboardFragment(), false, fadeLong)
                    else fm.popBackStack(false, fadeLong)
                }

                serviceReady = true
            }

        }
        */
        }
    }
}

@Composable
fun RoundSlider(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Double = 300.0,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Double = 180.0,
    onChange: (Double) -> Unit = {},
    strokeCap: StrokeCap = StrokeCap.Round,
    strokeWidth: Float = 10.dp.toPx(),
    pointerColor: Color = Color.Black,
    pointerRadius: Float = 15.dp.toPx(),
    colorList: List<Color>
) {
    val endAngle = (startAngle + sweepAngle) % 360

    var brush: Brush
    if (startAngle > endAngle) {
        val start = startAngle / 360
        val range = sweepAngle / 360
        val step = range / (colorList.size - 1)

        var colorList = colorList.toMutableList()
        var colors: MutableList<Pair<Float, Color>> = mutableListOf()

        var left = start - step

        for (i in 0..colorList.size) {
            if (left < 1) {
                left += step
                colors.add(Pair(left.toFloat(), colorList[0]))
                colorList.removeAt(0)
            } else break
        }

        left -= 1

        for (i in colorList.size - 1 downTo 0) {
            colors.add(0, Pair((left + step * i).toFloat(), colorList.last()))
            colorList.removeLast()
        }

        brush = Brush.sweepGradient(*colors.toTypedArray())
    } else {
        val start = startAngle / 360
        val range = sweepAngle / 360
        val step = range / (colorList.size - 1)

        var colors = Array(colorList.size) {
            Pair((start + step * it).toFloat(), colorList[it])
        }

        brush = Brush.sweepGradient(*colors)
    }

    RoundSlider(
        modifier,
        startAngle,
        sweepAngle,
        onChange,
        strokeCap,
        strokeWidth,
        pointerColor,
        pointerRadius,
        brush
    )
}

@Composable
fun RoundSlider(
    modifier: Modifier = Modifier,
    @FloatRange(from = 0.0, to = 360.0)
    startAngle: Double = 0.0,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Double = 360.0,
    onChange: (Double) -> Unit = {},
    strokeCap: StrokeCap = StrokeCap.Round,
    strokeWidth: Float = 10.dp.toPx(),
    pointerColor: Color = Color.Black,
    pointerRadius: Float = 15.dp.toPx(),
    brush: Brush
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(0f) }

    val endAngle = (startAngle + sweepAngle) % 360
    val midAngle = endAngle + (360.0 - sweepAngle) / 2.0

    Surface(
        modifier = modifier
    ) {
        Surface(modifier =
        Modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                position = change.position.div(scale)
                change.consumeAllChanges()
            }
        }
        ) { }

        Canvas(
            modifier = Modifier.padding((strokeWidth / 2).toDp())
        ) {
            //Draw path
            drawArc(
                brush = brush,
                startAngle = startAngle.toFloat(),
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Canvas(
            modifier = Modifier
                .padding((strokeWidth / 2).toDp())
                .onGloballyPositioned {
                    radius = minOf(it.size.width / 2, it.size.height / 2).toFloat()
                    scale = 1 + (strokeWidth / 2) / radius
                }
        ) {
            //Calculate angle
            var x = position.x - center.x
            var y = position.y - center.y
            val c = sqrt((x * x + y * y).toDouble())

            var _angle = Math.toDegrees(acos(x / c))
            if (y < 0) _angle = 360.0 - _angle

            //Keep in range
            if (sweepAngle != 360.0) {
                if (endAngle < startAngle) {
                    when (_angle) {
                        in endAngle..midAngle -> _angle = endAngle
                        in midAngle..startAngle -> _angle = startAngle
                    }
                } else if (endAngle > startAngle) {
                    if (midAngle > 360f) {
                        val correctedMiddle = midAngle - 360.0
                        when (_angle) {
                            in endAngle..360.0, in 0.0..correctedMiddle -> _angle = endAngle
                            in correctedMiddle..startAngle -> _angle = startAngle
                        }
                    } else {
                        when (_angle) {
                            in endAngle..midAngle -> _angle = endAngle
                            in midAngle..360.0, in 0.0..startAngle -> _angle = startAngle
                        }
                    }
                }
            }

            onChange(_angle)

            x = (center.x + radius * cos(Math.toRadians(_angle))).toFloat()
            y = (center.y + radius * sin(Math.toRadians(_angle))).toFloat()
            drawCircle(color = pointerColor, radius = pointerRadius, center = Offset(x, y))
        }
    }
}