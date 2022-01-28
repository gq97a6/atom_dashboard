package com.netDashboard

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import com.netDashboard.globals.G.mapper
import com.netDashboard.tile.TileView
import java.io.File
import java.io.FileReader

@Suppress("UNUSED")
class Theme {

    val a = Artist()

    fun apply(
        context: Context,
        viewGroup: ViewGroup,
        anim: Boolean = false,
        colorPallet: ColorPallet = a.colorPallet
    ) {
        context.setTheme(if (!a.isDark) R.style.Theme_Dark else R.style.Theme_Light)

        try {
            WindowInsetsControllerCompat(
                (context as Activity).window,
                viewGroup
            ).isAppearanceLightStatusBars = !a.isDark

            context.window.statusBarColor = colorPallet.background
        } catch (e: Exception) {

        }

        viewGroup.applyTheme(colorPallet)
        if (anim) viewGroup.applyAnimations()
    }

    private fun ViewGroup.applyTheme(p: ColorPallet) {
        for (i in 0 until this.childCount) {
            val v = this.getChildAt(i)

            if (v is ViewGroup) v.applyTheme(if (v is TileView) v.colorPallet else p)
            v.defineType(if (v is TileView) v.colorPallet else p)
        }

        this.defineType(p)
    }

    private fun ViewGroup.applyAnimations() {
        fun ViewGroup.apply() {
            if (this is ConstraintLayout || this is LinearLayout || this is FrameLayout) {
                this.layoutTransition = LayoutTransition()
                this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            }
        }

        for (i in 0 until this.childCount) {
            val v = this.getChildAt(i)
            if (v is ViewGroup) {
                v.applyAnimations()
                v.apply()
            }
        }

        this.apply()
    }

    private fun View.defineType(p: ColorPallet) {
        when (this) {
            is RadioButton -> this.applyTheme(p)
            is MaterialButton -> this.applyTheme(p)
            is SwitchMaterial -> this.applyTheme(p)
            is EditText -> this.applyTheme(p)
            is Chip -> this.applyTheme(p)
            is TextView -> this.applyTheme(p)
            is Slider -> this.applyTheme(p)
            is LinearLayout -> this.applyTheme(p)
            is FrameLayout -> this.applyTheme(p)
            is ConstraintLayout -> this.applyTheme(p)
            is RecyclerView -> this.applyTheme(p)
            is ChipGroup -> this.applyTheme(p)
            else -> {
                if (this.javaClass == View::class.java) this.applyTheme(p)
                else Log.i("OUY", "View type not specified: ${this.javaClass}")
            }
        }
    }

    private fun View.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "color" -> this.setBackgroundColor(p.color)
            "colorA" -> this.setBackgroundColor(p.a)
            "colorB" -> this.setBackgroundColor(p.b)
            "colorC" -> this.setBackgroundColor(p.c)
            "colorD" -> this.setBackgroundColor(p.d)
            "background" -> this.setBackgroundColor(p.background)
            "sliderBackground" -> {
                val drawable = GradientDrawable()
                drawable.mutate()
                drawable.setColor(p.d)
                drawable.cornerRadius = 15f
                this.background = drawable
            }
            "colorIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.color)
            "colorAIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.color)
            "groupArrow" -> this.backgroundTintList = ColorStateList.valueOf(p.color)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, p.color)
            }
            "sliderPopupFrame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(10, p.color)
                drawable?.cornerRadius = 25f
            }
            "rippleForeground" -> {
                val background = this.background as RippleDrawable
                background.setColor(ColorStateList.valueOf(p.background.alpha(150)))
            }
            else -> onUnknownTag(this.tag, "view")
        }
    }

    private fun FrameLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "background" -> this.setBackgroundColor(p.background)
            else -> onUnknownTag(this.tag, "frameLayout")
        }
    }

    private fun ConstraintLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "background" -> this.setBackgroundColor(p.background)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(6, p.color)
                drawable?.cornerRadius = 15f
            }
            "bar" -> this.backgroundTintList = ColorStateList.valueOf(contrastColor(!a.isDark, 200))
            else -> onUnknownTag(this.tag, "constraintLayout")
        }
    }

    private fun LinearLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "background" -> this.setBackgroundColor(p.background)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, p.color)
            }
            "groupBar" -> this.setBackgroundColor(p.background.contrast(!a.isDark, 0.3f))
            "group" -> this.setBackgroundColor(p.background.contrast(!a.isDark, 0.1f))
            else -> onUnknownTag(this.tag, "linearLayout")
        }
    }

    private fun RecyclerView.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "log" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(1, p.color)
            }
            else -> onUnknownTag(this.tag, "recyclerView")
        }
    }

    private fun MaterialButton.applyTheme(p: ColorPallet) {
        val background = this.background as LayerDrawable?
        val ripple = background?.findDrawableByLayerId(R.id.ripple) as RippleDrawable?
        background?.mutate()

        ripple?.setColor(
            ColorStateList.valueOf(
                when (this.tag) {
                    "color" -> contrastColor(!a.isDark, 80)
                    "colorA" -> contrastColor(!a.isDark, 70)
                    "colorB" -> contrastColor(!a.isDark, 60)
                    "colorC" -> contrastColor(!a.isDark, 50)
                    else -> contrastColor(!a.isDark, 70)
                }
            )
        )

        this.setTextColor(
            when (this.tag) {
                "color" -> p.background
                "colorA" -> blendARGB(p.color, p.background, 0.9f)
                "colorB" -> blendARGB(p.color, p.background, 0.1f)
                "colorC" -> p.color
                else -> p.color
            }
        )

        this.backgroundTintList = when (this.tag) {
            "color" -> ColorStateList.valueOf(p.color)
            "colorA" -> ColorStateList.valueOf(p.a)
            "colorB" -> ColorStateList.valueOf(p.b)
            "colorC" -> ColorStateList.valueOf(p.c)
            else -> ColorStateList.valueOf(p.c)
        }
    }

    private fun RadioButton.applyTheme(p: ColorPallet) {

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(p.c, p.b)
        )

        when (this.tag) {
            "default" -> {
                this.setTextColor(colorStateList)
                this.buttonTintList = colorStateList
            }
            else -> onUnknownTag(this.tag, "radioButton")
        }
    }

    private fun TextView.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "color" -> this.setTextColor(p.color)
            "colorA" -> this.setTextColor(p.a)
            "colorB" -> this.setTextColor(p.b)
            "colorC" -> this.setTextColor(p.c)
            "colorD" -> this.setTextColor(p.d)
            "colorBackground" -> this.setTextColor(p.background)
            "tag" -> {
                this.setTextColor(p.color)
                this.setBackgroundColor(p.d)
            }
            "log" -> {
                this.setTextColor(p.color)
                this.setBackgroundColor(p.background)
            }
            else -> onUnknownTag(this.tag, "textView")
        }
    }

    private fun EditText.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "basic" -> {
                this.setTextColor(p.b)
                this.setHintTextColor(p.c)
                this.setBackgroundColor(p.background.contrast(!a.isDark, 0.2f))
            }
            else -> onUnknownTag(this.tag, "editText")
        }
    }

    private fun SwitchMaterial.applyTheme(p: ColorPallet) {
        when (this.tag) {
            else -> onUnknownTag(this.tag, "switchMaterial")
        }

        val states = arrayOf(
            intArrayOf(-android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_checked)
        )

        val colors = intArrayOf(p.c, p.b)

        val list = ColorStateList(states, colors)
        this.trackTintList = list
        this.thumbTintList =
            ColorStateList.valueOf(
                p.c
            )
    }

    private fun Slider.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "enabled" -> {
                this.trackActiveTintList = ColorStateList.valueOf(p.b)
                this.tickActiveTintList = ColorStateList.valueOf(p.b)
                this.trackInactiveTintList = ColorStateList.valueOf(p.c)
                this.tickInactiveTintList = ColorStateList.valueOf(p.c)
                this.thumbTintList = ColorStateList.valueOf(p.color)
            }
            "disabled" -> {
                this.trackActiveTintList = ColorStateList.valueOf(p.c)
                this.tickActiveTintList = ColorStateList.valueOf(p.c)
                this.trackInactiveTintList = ColorStateList.valueOf(p.d)
                this.tickInactiveTintList = ColorStateList.valueOf(p.d)
                this.thumbTintList = ColorStateList.valueOf(p.b)
            }
            else -> onUnknownTag(this.tag, "slider")
        }
    }

    private fun Chip.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "colorA" -> {
                val colorStateListBackground = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ), intArrayOf(p.c, p.b)
                )

                val colorStateListText = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ), intArrayOf(p.color, p.color)
                )

                this.chipBackgroundColor = colorStateListBackground
                this.setTextColor(colorStateListText)
            }
            else -> onUnknownTag(this.tag, "chip")
        }
    }

    private fun ChipGroup.applyTheme(p: ColorPallet) {
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
                mapper.readValue(FileReader(FolderTree.themeFile), Theme::class.java)
            } catch (e: Exception) {
                Theme()
            }
    }

    fun save() {
        try {
            File(FolderTree.themeFile).writeText(mapper.writeValueAsString(this))
        } catch (e: Exception) {
            run { }
        }
    }

    inner class Artist {

        var isDark = true
            set(value) {
                field = value
                colorPallet = getColorPallet(hsv)
            }

        var colorPallet: ColorPallet

        var hsv: FloatArray = floatArrayOf(174f, 1f, 1f)
            set(value) {
                field = value
                colorPallet = getColorPallet(hsv)
            }

        init {
            colorPallet = getColorPallet(hsv)
        }

        fun getColorPallet(hsv: FloatArray, isAltCon: Boolean = false): ColorPallet {
            var c = 0
            var maxS: Float = 1f
            var maxV: Float = 1f
            var minV: Float = 0f

            val colorBackground = if (isDark) Color.rgb(90, 90, 90)
            else Color.rgb(200, 200, 200)

            //Compute maximal saturation/value
            for (i in 100 downTo 0) {
                c = Color.HSVToColor(
                    floatArrayOf(
                        hsv[0],
                        if (isDark) i / 100f else hsv[1],
                        if (isDark) 1f else i / 100f
                    )
                )

                if (ColorUtils.calculateContrast(c, colorBackground) > if (isAltCon) 1.7 else 2.7) {
                    maxS = if (isDark) i / 100f else 1f
                    maxV = if (isDark) 1f else i / 100f
                    break
                }
            }

            //Compute minimal value
            if (!isDark) minV = 0f
            else {
                for (i in 100 downTo 0) {
                    if (ColorUtils.calculateContrast(c, colorBackground) < 3.6) {
                        minV = i / 100f
                        break
                    }

                    c = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], i / 100f))
                }
            }

            val color = Color.HSVToColor(
                floatArrayOf(
                    hsv[0],
                    maxS * hsv[1],
                    minV + (maxV - minV) * hsv[2]
                )
            )

            return ColorPallet(
                color,
                colorBackground,
                blendARGB(color, colorBackground, 0.35f),
                blendARGB(color, colorBackground, 0.55f),
                blendARGB(color, colorBackground, 0.75f),
                blendARGB(color, colorBackground, 0.85f)
            )
        }
    }

    class ColorPallet(
        val color: Int,
        val background: Int,
        val a: Int,
        val b: Int,
        val c: Int,
        val d: Int
    )
}