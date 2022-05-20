package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.FloatRange
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
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
                        Surface(
                            modifier = Modifier
                                .size(300.dp)
                                .background(color = Color.White)
                        ) {
                            RoundSlider(modifier = Modifier.padding(20.dp))
                        }
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
    @FloatRange(from = -360.0, to = 360.0)
    startAngle: Double = 90.0,
    @FloatRange(from = 0.0, to = 360.0)
    sweepAngle: Double = 90.0
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var radius by remember { mutableStateOf(0f) }
    val endAngle by remember { mutableStateOf(startAngle + sweepAngle) }
    var angle by remember { mutableStateOf(startAngle) }

    Canvas(
        modifier = modifier
            .background(color = Color.Green.copy(alpha = 0.2f))
            .onGloballyPositioned {
                radius = minOf(it.size.width / 2, it.size.height / 2).toFloat()
            }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    position = change.position
                    change.consumeAllChanges()
                }
            }
    ) {
        //Draw path
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red,
                    Color.Red,
                    Color.Red,
                    Color.Red,
                    Color.Red,
                    Color.Red,
                    Color.Red,
                    Color.Red
                ),
                center = center
            ),
            startAngle = startAngle.toFloat(),
            sweepAngle = sweepAngle.toFloat(),
            useCenter = false,
            style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
        )

        //Calculate angle
        var x = position.x - center.x
        var y = position.y - center.y
        val c = sqrt((x * x + y * y).toDouble())

        var _angle = Math.toDegrees(acos(x / c))
        if (y < 0) _angle = 360.0 - _angle

        //Keep in range
        if (sweepAngle != 360.0 && _angle !in startAngle..endAngle) {
            val middleAngle = startAngle + sweepAngle + (360f - sweepAngle) / 2f
            _angle = if (_angle > middleAngle) startAngle else endAngle
        }

        angle = _angle

        x = (center.x + radius * cos(Math.toRadians(angle))).toFloat()
        y = (center.y + radius * sin(Math.toRadians(angle))).toFloat()
        drawCircle(color = Color.Black, radius = 15.dp.toPx(), center = Offset(x, y))
    }
}

fun DrawScope.drawOnAngle(angle: Double, radius: Float) {
    val x = (center.x + radius * cos(Math.toRadians(angle))).toFloat()
    val y = (center.y + radius * sin(Math.toRadians(angle))).toFloat()
    drawCircle(color = Color.Black, radius = 15.dp.toPx(), center = Offset(x, y))
}