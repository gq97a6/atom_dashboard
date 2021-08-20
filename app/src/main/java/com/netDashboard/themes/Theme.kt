@file:Suppress("ControlFlowWithEmptyBody")

package com.netDashboard.themes

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.netDashboard.*

object Theme {

    var color = Color.parseColor("#d6d6bf")

    private val colorA
        get() = ColorUtils.blendARGB(color, color.contrast(), 0.2f)
    private val colorB
        get() = ColorUtils.blendARGB(color, color.contrast(), 0.4f)
    private val colorC
        get() = ColorUtils.blendARGB(color, color.contrast(), 0.6f)
    private val colorD
        get() = ColorUtils.blendARGB(color, color.contrast(), 0.8f)

    private val isDark
        get() = color.isDark()

    private val colorBackground: Int
        get() {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)
            hsv[1] = (100 * hsv[1]) / ((300 * hsv[1]) + 100).roundCloser(0.01f)
            hsv[2] = if (color.isDark()) 0.8f else 0.5f

            return Color.HSVToColor(hsv)
        }

    fun apply(context: Context, viewGroup: ViewGroup) {

        context.setTheme(if (isDark) R.style.Theme_Dark else R.style.Theme_Light)

        WindowInsetsControllerCompat(
            (context as Activity).window,
            viewGroup
        ).isAppearanceLightStatusBars = !color.isDark()

        context.window.statusBarColor = colorBackground

        viewGroup.applyTheme()
    }

    private fun ViewGroup.applyTheme() {
        for (i in 0 until this.childCount) {
            val v = this.getChildAt(i)

            if (v is ViewGroup) v.applyTheme()
            v.defineType()
        }

        this.defineType()
    }

    private fun View.defineType() {
        when (this) {
            is MaterialButton -> this.applyTheme()
            is ImageView -> this.applyTheme()
            is TextView -> this.applyTheme()
            is SwitchMaterial -> this.applyTheme()
            is EditText -> this.applyTheme()
            is Chip -> this.applyTheme()
            is Slider -> this.applyTheme()
            is LinearLayout -> this.applyTheme()
            is FrameLayout -> this.applyTheme()
            is RecyclerView -> this.applyTheme()
            is ChipGroup -> this.applyTheme()
            else -> {
                if (this.javaClass == View::class.java) this.applyTheme()
                else Log.i("OUY", "View type not specified: ${this.javaClass}")
            }
        }
    }

    private fun ChipGroup.applyTheme() {
        when (this.tag) {
        }
    }

    private fun RecyclerView.applyTheme() {
        when (this.tag) {
        }
    }

    private fun FrameLayout.applyTheme() {
        when (this.tag) {
        }
    }

    private fun LinearLayout.applyTheme() {
        when (this.tag) {
            "background" -> this.setBackgroundColor(colorBackground)
        }
    }

    private fun View.applyTheme() {
        when (this.tag) {
            "color" -> this.setBackgroundColor(color)
            "colorA" -> this.setBackgroundColor(colorA)
            "colorB" -> this.setBackgroundColor(colorB)
            "colorC" -> this.setBackgroundColor(colorC)
            "colorD" -> this.setBackgroundColor(colorD)
        }
    }

    private fun MaterialButton.applyTheme() {
        when (this.tag) {
        }
        this.backgroundTintList = ColorStateList.valueOf(color)
    }

    private fun ImageView.applyTheme() {
        when (this.tag) {
        }
        //this.backgroundTintList =
        //    ColorStateList.valueOf(
        //        getRandomColor()
        //    )
    }

    private fun TextView.applyTheme() {
        when (this.tag) {
        }
        this.setTextColor(colorA)
    }

    private fun SwitchMaterial.applyTheme() {
        when (this.tag) {
        }
        this.backgroundTintList =
            ColorStateList.valueOf(
                getRandomColor()
            )
    }

    private fun EditText.applyTheme() {
        when (this.tag) {
        }
        this.setTextColor(colorD)
    }

    private fun Chip.applyTheme() {
        when (this.tag) {
        }
        this.backgroundTintList =
            ColorStateList.valueOf(
                getRandomColor()
            )
    }

    private fun Slider.applyTheme() {
        when (this.tag) {
        }
        this.trackActiveTintList = ColorStateList.valueOf(colorD)
    }
}