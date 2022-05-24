package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.ArcSlider
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.toPx

class ThemeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ArcSlider(
                            modifier = Modifier.size(250.dp),
                            startAngle = 100.0,
                            sweepAngle = 150.0,
                            strokeWidth = 15.dp.toPx(),
                            pointerRadius = 15.dp.toPx(),
                            //pointerStyle = Stroke(width = 2.dp.toPx()),
                            pointerColor = Color.Gray,
                            colorList = listOf(
                                Color.Red,
                                Color.Yellow,
                                Color.Green,
                                Color.Cyan,
                                Color.Blue,
                                Color.Magenta,
                                Color.Red
                            )
                        )

                        //ArcSlider(
                        //    modifier = Modifier.size(200.dp),
                        //    startAngle = 100.0,
                        //    sweepAngle = 160.0,
                        //    strokeWidth = 15.dp.toPx(),
                        //    pointerRadius = 15.dp.toPx(),
                        //    pointerStyle = Stroke(width = 1.dp.toPx()),
                        //    pointerColor = Color.Gray,
                        //    colorList = listOf(
                        //        Color.Red,
                        //        Color.Yellow,
                        //        Color.Green,
                        //        Color.Cyan,
                        //        Color.Blue,
                        //        Color.Magenta,
                        //        Color.Red
                        //    )
                        //)
//
                        //ArcSlider(
                        //    modifier = Modifier.size(150.dp),
                        //    startAngle = 0.0,
                        //    sweepAngle = 360.0,
                        //    strokeWidth = 15.dp.toPx(),
                        //    pointerRadius = 15.dp.toPx(),
                        //    pointerStyle = Stroke(width = 1.dp.toPx()),
                        //    pointerColor = Color.Gray,
                        //    colorList = listOf(
                        //        Color.Red,
                        //        Color.Yellow,
                        //        Color.Green,
                        //        Color.Cyan,
                        //        Color.Blue,
                        //        Color.Magenta,
                        //        Color.Red
                        //    )
                        //)

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