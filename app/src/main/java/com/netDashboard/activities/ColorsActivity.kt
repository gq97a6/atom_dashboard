package com.netDashboard.activities

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.netDashboard.*
import com.netDashboard.databinding.ActivityColorsBinding

class ColorsActivity : AppCompatActivity() {
    private lateinit var b: ActivityColorsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityColorsBinding.inflate(layoutInflater)
        setContentView(b.root)

        setHSVGradient()
    }

    fun restart(view: View) = this.recreate()

    private fun setHSVGradient(color: Int = getRandomColor()) {

        val dark = intArrayOf(
            color.darkened(0f),
            color.darkened(0.1f),
            color.darkened(0.2f),
            color.darkened(0.3f),
            color.darkened(0.4f),
            color.darkened(0.5f),
            color.darkened(0.6f),
            color.darkened(0.7f),
            color.darkened(0.8f),
            color.darkened(0.9f),
            color.darkened(1f)
        )

        val light = intArrayOf(
            color.lightened(0f),
            color.lightened(0.1f),
            color.lightened(0.2f),
            color.lightened(0.3f),
            color.lightened(0.4f),
            color.lightened(0.5f),
            color.lightened(0.6f),
            color.lightened(0.7f),
            color.lightened(0.8f),
            color.lightened(0.9f),
            color.lightened(1f)
        )

        if (color.isDark()) {
            b.dark.setGradientSliderBackground(dark)
        } else {
            b.dark.setGradientSliderBackground(light)
        }
    }

    private fun View.setGradientSliderBackground(colors: IntArray) {

        val background = ResourcesCompat.getDrawable(
            resources,
            R.drawable.background_bw_slider,
            null
        ) as GradientDrawable

        background.mutate()
        background.colors = colors

        this.background = InsetDrawable(
            background,
            (14.5f).toPx(),
            (22.5f).toPx(),
            (14.5f).toPx(),
            (22f).toPx()
        )
    }
}