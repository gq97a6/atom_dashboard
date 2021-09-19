package com.netDashboard.theme

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.netDashboard.*
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.globals.G.gson
import java.io.File
import java.io.FileReader
import kotlin.random.Random

@Suppress("UNUSED")
class Theme {

    var useOver = false
    var isDark = false
    var color = Color.parseColor("#00469c")

    private val isDarkRec: Boolean
        get() {
            val lConCheck = (ColorUtils.calculateContrast(color, getBackground(false)) > 2.45)
            val dConCheck = (ColorUtils.calculateContrast(color, getBackground(true)) > 1.4)

            return if (lConCheck && dConCheck) isDark else lConCheck
        }

    private val colorA
        get() = ColorUtils.blendARGB(color, colorBackground, 0.4f)
    private val colorB
        get() = ColorUtils.blendARGB(color, colorBackground, 0.6f)
    private val colorC
        get() = ColorUtils.blendARGB(color, colorBackground, 0.8f)
    val colorD
        get() = ColorUtils.blendARGB(color, colorBackground, 0.9f)

    private val colorBackground: Int
        get() = getBackground(!isDark)

    private fun getBackground(isDark: Boolean): Int {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(color, hsv)
        hsv[1] = hsv[1] * 0.3f

        return Color.HSVToColor(hsv).contrast(isDark, 0.6f)
    }

    fun apply(context: Context, viewGroup: ViewGroup) {
        //setRandomTheme()

        context.setTheme(if (!isDark) R.style.Theme_Dark else R.style.Theme_Light)

        try {
            WindowInsetsControllerCompat(
                (context as Activity).window,
                viewGroup
            ).isAppearanceLightStatusBars = !isDark

            context.window.statusBarColor = colorBackground
        } catch (e: Exception) {

        }

        viewGroup.applyTheme()
    }

    private fun setRandomTheme() {
        color = getRandomColor()
        isDark = Random.nextBoolean()

        if (isDark != isDarkRec) setRandomTheme()
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
            is SwitchMaterial -> this.applyTheme()
            is EditText -> this.applyTheme()
            is TextView -> this.applyTheme()
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

    private fun View.applyTheme() {
        when (this.tag) {
            "color" -> this.setBackgroundColor(color)
            "colorA" -> this.setBackgroundColor(colorA)
            "colorB" -> this.setBackgroundColor(colorB)
            "colorC" -> this.setBackgroundColor(colorC)
            "bar" -> this.backgroundTintList = ColorStateList.valueOf(
                contrastColor(!isDark, 205)
            )
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(1, color)
            }
            "group_arrow" -> this.backgroundTintList = ColorStateList.valueOf(color)
            "foreground" -> {
                val background = this.background as RippleDrawable
                background.setColor(ColorStateList.valueOf(colorBackground))
            }
            else -> onUnknownTag(this.tag, "view")
        }
    }

    private fun FrameLayout.applyTheme() {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "frameLayout")
        }

        this.layoutTransition = LayoutTransition()
        this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun LinearLayout.applyTheme() {
        when (this.tag) {
            "background" -> this.setBackgroundColor(colorBackground)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(1, color)
            }
            "group_bar" -> this.setBackgroundColor(colorBackground.contrast(!isDark, 0.3f))
            "group" -> this.setBackgroundColor(colorBackground.contrast(!isDark, 0.1f))
            else -> onUnknownTag(this.tag, "linearLayout")
        }

        this.layoutTransition = LayoutTransition()
        this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
    }

    private fun RecyclerView.applyTheme() {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "recyclerView")
        }
    }

    private fun MaterialButton.applyTheme() {
        when (this.tag) {
            "color" -> this.backgroundTintList = ColorStateList.valueOf(color)
            "colorA" -> this.backgroundTintList = ColorStateList.valueOf(colorA)
            "item" -> {
                this.backgroundTintList = ColorStateList.valueOf(colorB)
                this.setTextColor(color)
                this.rippleColor = ColorStateList.valueOf(colorBackground)
            }
            "tile_button" -> {
                this.backgroundTintList = ColorStateList.valueOf(colorB)
                this.setTextColor(color)
                this.rippleColor = ColorStateList.valueOf(colorBackground)
            }
            else -> onUnknownTag(this.tag, "materialButton")
        }
    }

    private fun TextView.applyTheme() {
        when (this.tag) {
            "color" -> this.setTextColor(color)
            "colorA" -> this.setTextColor(colorA)
            "colorB" -> this.setTextColor(colorB)
            "colorC" -> this.setTextColor(colorC)
            "tag" -> {
                this.setTextColor(color)
                this.setBackgroundColor(colorD)
            }
            "button" -> {
                this.setTextColor(color)
                this.backgroundTintList = ColorStateList.valueOf(colorB)
            }
            "con_warning" -> {
                this.clearAnimation()
                this.visibility = if (isDark != isDarkRec) {
                    this.setTextColor(color.contrast(isDark, 0.6f))
                    this.blink(-1, 20, 500)

                    VISIBLE
                } else {
                    GONE
                }
            }
            else -> onUnknownTag(this.tag, "textView")
        }
    }

    private fun EditText.applyTheme() {
        when (this.tag) {
            "basic" -> {
                this.setTextColor(colorA)
                this.setHintTextColor(colorC)
                this.setBackgroundColor(colorBackground.contrast(!isDark, 0.2f))
            }
            else -> onUnknownTag(this.tag, "editText")
        }
    }

    private fun SwitchMaterial.applyTheme() {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "switchMaterial")
        }

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )

        val colors = intArrayOf(
            colorC,
            colorB
        )

        val list = ColorStateList(states, colors)
        this.trackTintList = list
        this.thumbTintList =
            ColorStateList.valueOf(
                color
            )
    }

    private fun Slider.applyTheme() {
        when (this.tag) {
            "basic" -> {
                this.trackActiveTintList = ColorStateList.valueOf(colorB)
                this.tickActiveTintList = ColorStateList.valueOf(colorB)
                this.trackInactiveTintList = ColorStateList.valueOf(colorC)
                this.tickInactiveTintList = ColorStateList.valueOf(colorC)
                this.thumbTintList = ColorStateList.valueOf(color)
            }
            else -> onUnknownTag(this.tag, "slider")
        }
    }

    private fun Chip.applyTheme() {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "chip")
        }
        this.backgroundTintList =
            ColorStateList.valueOf(
                getRandomColor()
            )
    }

    private fun ChipGroup.applyTheme() {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "chipGroup")
        }
    }

    private fun onUnknownTag(tag: Any?, type: String) {
        tag?.toString()?.let {
            if (it.isNotBlank()) Log.i("OUY", "Unknown $type tag: $it")
        }
    }

    companion object {
        fun getSaved(): Theme =
            try {
                gson.fromJson(FileReader(FolderTree.themeFile), Theme::class.java)
            } catch (e: Exception) {
                Theme()
            }
    }

    fun save() {
        try {
            File(FolderTree.themeFile).writeText(gson.toJson(this))
        } catch (e: Exception) {
            throw e
        }
    }
}