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
import com.netDashboard.globals.G.theme
import java.io.File
import java.io.FileReader

@Suppress("UNUSED")
class Theme {

    val a = Artist()

    companion object {
        fun Theme.saveToFile(save: String = this.prepareSave()) {
            try {
                File(FolderTree.themeFile).writeText(save)
            } catch (e: Exception) {
                run { }
            }
        }

        fun getSaveFromFile() = try {
            FileReader(FolderTree.themeFile).readText()
        } catch (e: Exception) {
            ""
        }

        fun parseSave(save: String = getSaveFromFile()): Theme? =
            try {
                mapper.readValue(save, Theme::class.java)
            } catch (e: Exception) {
                null
            }
    }

    fun apply(
        viewGroup: ViewGroup,
        context: Context? = null,
        anim: Boolean = true,
        colorPallet: ColorPallet = a.colorPallet
    ) {
        context?.let {
            it.setTheme(if (!a.isDark) R.style.Theme_Dark else R.style.Theme_Light)

            try {
                WindowInsetsControllerCompat(
                    (it as Activity).window,
                    viewGroup
                ).isAppearanceLightStatusBars = !a.isDark

                it.window.statusBarColor = colorPallet.background
            } catch (e: Exception) {

            }
        }

        viewGroup.applyTheme(colorPallet)
        if (anim) viewGroup.applyAnimations()
    }

    private fun ViewGroup.applyTheme(p: ColorPallet) {
        for (i in 0 until this.childCount) {
            val v = this.getChildAt(i)

            if (v is ViewGroup) v.applyTheme(p)
            v.defineType(p)
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
            is CheckBox -> this.applyTheme(p)
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
            "colorAIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.a)
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
                background?.mutate()
                background?.setColor(ColorStateList.valueOf(theme.a.colorPallet.background.alpha(150)))
            }
            "rippleForegroundDim" -> {
                val background = this.background as RippleDrawable
                background?.mutate()
                background?.setColor(
                    ColorStateList.valueOf(
                        theme.a.colorPallet.background.darkened(
                            0.9f
                        ).alpha(150)
                    )
                )
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
                drawable?.setStroke(2, p.color)
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
                drawable?.setStroke(2, p.color)
            }
            "groupBar" -> this.setBackgroundColor(Color.TRANSPARENT)
            "group" -> this.setBackgroundColor(Color.TRANSPARENT)
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

        ripple?.setColor(ColorStateList.valueOf(contrastColor(!a.isDark)))

        this.backgroundTintList = when (this.tag) {
            "color" -> ColorStateList.valueOf(p.color)
            "colorA" -> ColorStateList.valueOf(p.a)
            "colorC" -> ColorStateList.valueOf(p.c)
            "colorD" -> ColorStateList.valueOf(p.d)
            else -> ColorStateList.valueOf(p.c)
        }

        this.setTextColor(
            when (this.tag) {
                "color" -> p.d
                "colorA" -> blendARGB(p.d, p.color, .1f)
                "colorC" -> blendARGB(p.d, p.color, .9f)
                "colorD" -> p.color
                else -> blendARGB(p.d, p.color, .9f)
            }
        )
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

    private fun CheckBox.applyTheme(p: ColorPallet) {

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
            "frame" -> {
                this.setTextColor(p.color)
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, p.color)
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
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, p.c)
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
        val colors = intArrayOf(p.c, p.a)
        val list = ColorStateList(states, colors)

        this.trackTintList = list
        this.thumbTintList = ColorStateList.valueOf(p.color)
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

    inner class Artist {

        var isDark = true
            set(value) {
                field = value
                colorPallet = getColorPallet(hsv)
            }

        var colorPallet: ColorPallet

        var hsv: FloatArray = floatArrayOf(0f, 0f, 0f)
            set(value) {
                field = value
                colorPallet = getColorPallet(hsv)
            }

        init {
            colorPallet = getColorPallet(hsv)
        }

        fun parseColor(color: Int, isAltCon: Boolean = false): Int {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)

            return getColorPallet(hsv, isAltCon).color
        }

        fun getColorPallet(
            hsv: FloatArray,
            isAltCon: Boolean = false,
            isRaw: Boolean = false
        ): ColorPallet {

            val color: Int
            val colorBackground = if (isDark) Color.rgb(20, 20, 20)
            else Color.rgb(240, 240, 240)

            if (!isRaw) {
                var col = 0
                var maxS: Float = 1f
                var maxV: Float = 1f
                var minV: Float = 0f

                //Compute maximal saturation/value
                for (i in 100 downTo 0) {
                    col = Color.HSVToColor(
                        floatArrayOf(
                            hsv[0],
                            if (isDark) i / 100f else hsv[1],
                            if (isDark) 1f else i / 100f
                        )
                    )

                    if (ColorUtils.calculateContrast(
                            col,
                            colorBackground
                        ) > if (isAltCon) 1.7 else 5.0
                    ) {
                        maxS = if (isDark) i / 100f else 1f
                        maxV = if (isDark) 1f else i / 100f
                        break
                    }
                }

                //Compute minimal value
                if (!isDark) minV = 0f
                else {
                    for (i in 100 downTo 0) {
                        if (ColorUtils.calculateContrast(col, colorBackground) < 3.6) {
                            minV = i / 100f
                            break
                        }

                        col = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], i / 100f))
                    }
                }

                color = Color.HSVToColor(
                    floatArrayOf(
                        hsv[0],
                        maxS * hsv[1],
                        minV + (maxV - minV) * if (isDark) 1f else hsv[2]
                    )
                )
            } else color = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], hsv[2]))

            val a = blendARGB(color, colorBackground, 0.2f)
            val b = blendARGB(a, colorBackground, 0.35f)
            val c = blendARGB(b, colorBackground, 0.45f)
            val d = blendARGB(c, colorBackground, 0.55f)

            return ColorPallet(color, colorBackground, a, b, c, d)
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