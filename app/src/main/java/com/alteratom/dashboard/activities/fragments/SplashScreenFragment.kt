package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.ArcSlider
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.toPx

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
                        ArcSlider(
                            modifier = Modifier
                                .padding(0.dp)
                                .size(200.dp),
                            startAngle = 0.0,
                            sweepAngle = 180.0,
                            strokeWidth = 20.dp.toPx(),
                            pointerRadius = 15.dp.toPx(),
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
                            onChange = {
                                Log.i("OUY", "$it")
                            }
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