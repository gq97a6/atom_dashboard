package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme
import kotlin.math.*

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
fun RoundSlider(modifier: Modifier = Modifier) {
    var dragPosition by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                dragPosition += dragAmount
                change.consumeAllChanges()
            }
        }.background(color = Color.Green.copy(alpha = 0.2f))
    ) {
        val xCenter = size.width / 2
        val yCenter = size.height / 2

        fun outerCircleRadius() = minOf(xCenter, yCenter)

        fun calculateIndicatorPosition(dragPosition: Offset): Offset {
            val dragXOnCanvas = dragPosition.x - xCenter
            val dragYOnCanvas = dragPosition.y - yCenter

            drawCircle(color = Color.Yellow, radius = 15.dp.toPx(), center = Offset(dragXOnCanvas, dragYOnCanvas))
            drawCircle(color = Color.Red, radius = 15.dp.toPx(), center = dragPosition)

            val radius = sqrt(dragXOnCanvas.pow(2) + dragYOnCanvas.pow(2))
            val angle = acos(dragXOnCanvas / radius)
            val adjustedAngle = if (dragYOnCanvas < 0) angle * -1 else angle

            val xOnCircle = outerCircleRadius() * cos(adjustedAngle)
            val yOnCircle = outerCircleRadius() * sin(adjustedAngle)

            return Offset(xOnCircle, yOnCircle)
        }

        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red),
                center = center
            ),
            radius = outerCircleRadius(),
            style = Stroke(width = 10.dp.toPx())
        )

        val (indicatorX, indicatorY) = calculateIndicatorPosition(dragPosition)
        translate(indicatorX, indicatorY) {
            drawCircle(color = Color.Black, radius = 15.dp.toPx())
        }
    }
}