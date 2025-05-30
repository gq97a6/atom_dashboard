package com.alteratom.dashboard

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.ColorUtils.blendARGB
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.RecyclerView
import com.alteratom.R
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.helper_objects.Debug
import com.fasterxml.jackson.annotation.JsonIgnore
import com.google.android.material.chip.Chip
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import me.tankery.lib.circularseekbar.CircularSeekBar
import androidx.compose.ui.graphics.Color as ComposeColor

@Suppress("UNUSED")
class Theme {

    val artist = Artist()

    companion object {
        val colors
            get() = aps.theme.artist.pallet.cc

        val isDark
            get() = aps.theme.artist.isDark
    }

    fun apply(
        viewGroup: ViewGroup? = null,
        context: Context? = null,
        anim: Boolean = true,
        colorPallet: ColorPallet = artist.pallet
    ) {
        (context as? Activity?)?.let {
            it.setTheme(R.style.theme)

            try {
                WindowInsetsControllerCompat(
                    it.window,
                    it.window.decorView
                ).isAppearanceLightStatusBars = !isDark

                //it.window.statusBarColor = colorPallet.background
                //it.window.navigationBarColor = colorPallet.background
            } catch (_: Exception) {
            }
        }

        viewGroup?.let {
            it.applyTheme(colorPallet)
            if (anim) it.applyAnimations()
        }
    }

    private fun ViewGroup.applyTheme(p: ColorPallet) {
        for (i in 0 until this.childCount) {
            this.getChildAt(i).let {
                if (it is ViewGroup) it.applyTheme(p)
                it.defineType(p)
            }
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
            is Chip -> this.applyTheme(p)
            is CheckBox -> this.applyTheme(p)
            is SwitchMaterial -> this.applyTheme(p)
            is Button -> this.applyTheme(p)
            is EditText -> this.applyTheme(p)
            is TextView -> this.applyTheme(p)
            is Slider -> this.applyTheme(p)
            is LinearLayout -> this.applyTheme(p)
            is FrameLayout -> this.applyTheme(p)
            is ConstraintLayout -> this.applyTheme(p)
            is RecyclerView -> this.applyTheme(p)
            is CircularSeekBar -> this.applyTheme(p)
            else -> if (this.javaClass == View::class.java) this.applyTheme(p)
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
            "background200" -> this.setBackgroundColor(p.background.alpha(200))
            "sliderBackground" -> {
                val drawable = GradientDrawable()
                drawable.setColor(p.d)
                drawable.cornerRadius = 15f
                this.background = drawable
            }

            "colorIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.color)
            "colorAIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.a)
            "colorCIcon" -> this.backgroundTintList = ColorStateList.valueOf(p.c)
            "groupArrow" -> this.backgroundTintList = ColorStateList.valueOf(p.color)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(3, p.color)
            }

            "sliderPopupFrame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(10, p.color)
                drawable?.cornerRadius = 25f
            }

            "rippleForeground" -> {
                val background = this.background as RippleDrawable
                background.setColor(
                    ColorStateList.valueOf(
                        aps.theme.artist.pallet.background.alpha(
                            150
                        )
                    )
                )
            }

            "rippleForegroundDim" -> {
                val background = this.background as RippleDrawable
                background.setColor(
                    ColorStateList.valueOf(
                        aps.theme.artist.pallet.background.darkened(
                            0.9f
                        ).alpha(150)
                    )
                )
            }

            "splashIcon" -> {
                this.setBackgroundResource(if (isDark) R.drawable.ic_icon_light else R.drawable.ic_icon)
                this.background.colorFilter =
                    PorterDuffColorFilter(artist.pallet.color.alpha(100), PorterDuff.Mode.SRC_ATOP)
            }

            else -> onUnknownTag(this.tag, "view")
        }
    }

    private fun FrameLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "corners" -> this.backgroundTintList = ColorStateList.valueOf(p.d.alpha(120))
            "background" -> this.setBackgroundColor(p.background)
            else -> onUnknownTag(this.tag, "frameLayout")
        }
    }

    private fun ConstraintLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "background" -> this.setBackgroundColor(p.background)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(3, p.color)
            }

            "bar" -> this.backgroundTintList =
                ColorStateList.valueOf(contrastColor(!artist.isDark, 100))

            else -> onUnknownTag(this.tag, "constraintLayout")
        }
    }

    private fun LinearLayout.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "background" -> this.setBackgroundColor(p.background)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(3, p.color)
            }

            "backgroundTint" -> this.backgroundTintList =
                ColorStateList.valueOf(p.background)

            "groupBar" -> this.setBackgroundColor(Color.TRANSPARENT)
            "group" -> this.setBackgroundColor(Color.TRANSPARENT)
            else -> onUnknownTag(this.tag, "linearLayout")
        }
    }

    private fun RecyclerView.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "log" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(3, p.color)
                val bar = getDrawable(context, R.drawable.bg_bar)
                bar?.setTint(p.b)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    this.verticalScrollbarThumbDrawable = bar
                }
            }

            else -> onUnknownTag(this.tag, "recyclerView")
        }
    }

    private fun Button.applyTheme(p: ColorPallet) {
        val ripple = this.foreground as? RippleDrawable?
        val stroke = ripple?.findDrawableByLayerId(R.id.stroke) as? GradientDrawable?

        ripple?.setColor(ColorStateList.valueOf(p.background))
        stroke?.setStroke(
            2f.toPx(), when (this.tag) {
                "color" -> ColorStateList.valueOf(p.color)
                "colorA" -> ColorStateList.valueOf(p.a)
                "colorB" -> ColorStateList.valueOf(p.b)
                "colorC" -> ColorStateList.valueOf(p.c)
                "colorD" -> ColorStateList.valueOf(p.d)
                else -> ColorStateList.valueOf(p.d)
            }
        )

        if (this.text.isEmpty()) {
            this.foregroundTintList = when (this.tag) {
                "color" -> ColorStateList.valueOf(p.color)
                "colorA" -> ColorStateList.valueOf(p.a)
                "colorB" -> ColorStateList.valueOf(p.b)
                "colorC" -> ColorStateList.valueOf(p.c)
                "colorD" -> ColorStateList.valueOf(p.d)
                else -> ColorStateList.valueOf(p.d)
            }
        }

        this.setTextColor(
            when (this.tag) {
                "color" -> p.color
                "colorA" -> p.a
                "colorB" -> p.b
                "colorC" -> p.c
                "colorD" -> p.d
                else -> p.d
            }
        )
    }

    private fun RadioButton.applyTheme(p: ColorPallet) {

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(p.c, p.a)
        )

        when (this.tag) {
            "default" -> {
                this.setTextColor(colorStateList)
                this.buttonTintList = colorStateList
                this.background = null
            }

            else -> onUnknownTag(this.tag, "radioButton")
        }
    }

    private fun CheckBox.applyTheme(p: ColorPallet) {

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(p.c, p.a)
        )

        when (this.tag) {
            "default" -> {
                this.setTextColor(colorStateList)
                this.buttonTintList = colorStateList
                this.background = null
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
                drawable?.setStroke(3, p.color)
            }

            "insert" -> {
                this.setTextColor(p.b)
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(3, p.c)
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
                drawable?.setStroke(3, p.c)
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
        val colors = intArrayOf(p.d, p.b)
        val list = ColorStateList(states, colors)

        this.trackTintList = list
        this.thumbTintList = ColorStateList.valueOf(p.color)
        this.background = null
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

    private fun CircularSeekBar.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "base" -> {
                this.circleProgressColor = p.b
                this.pointerColor = p.color
            }

            "goal" -> {
                this.circleProgressColor = p.b
            }

            "progress" -> {
                this.circleProgressColor = p.color
            }

            "track" -> {
                this.circleColor = p.c
            }

            else -> onUnknownTag(this.tag, "crcularSeekBar")
        }
    }

    private fun Chip.applyTheme(p: ColorPallet) {
        when (this.tag) {
            "colorA" -> {
                val colorStateListBackground = ColorStateList(
                    arrayOf(
                        intArrayOf(-android.R.attr.state_checked),
                        intArrayOf(android.R.attr.state_checked)
                    ), intArrayOf(p.c, p.a)
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

    private fun onUnknownTag(tag: Any?, type: String) {
        tag?.toString()?.let {
            //if (it.isNotBlank())
        }
    }

    inner class Artist {

        var isDark = true
            set(value) {
                field = value
                pallet = getColorPallet(hsv)
            }

        var hsv: FloatArray = floatArrayOf(0f, 0f, 1f)
            set(value) {
                field = value
                pallet = getColorPallet(hsv)
            }

        @JsonIgnore
        var pallet: ColorPallet = getColorPallet(hsv)

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
            else Color.rgb(230, 230, 230)

            if (!isRaw) {
                var col = 0
                var maxS = 1f
                var maxV = 1f
                var minV = 0f
                val hsv = if (isDark) floatArrayOf(hsv[0], hsv[1], 1f) else hsv

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

            val r = if (isDark) .4f else .25f
            val a = blendARGB(color, colorBackground, r)
            val b = blendARGB(a, colorBackground, r)
            val c = blendARGB(b, colorBackground, r)
            val d = blendARGB(c, colorBackground, r)

            return ColorPallet(color, colorBackground, a, b, c, d)
        }
    }

    class ComposeColorPallet(
        val color: ComposeColor,
        val background: ComposeColor,
        val a: ComposeColor,
        val b: ComposeColor,
        val c: ComposeColor,
        val d: ComposeColor
    )

    class ColorPallet(
        val color: Int,
        val background: Int,
        val a: Int,
        val b: Int,
        val c: Int,
        val d: Int
    ) {
        val cc = ComposeColorPallet(
            ComposeColor(color),
            ComposeColor(background),
            ComposeColor(a),
            ComposeColor(b),
            ComposeColor(c),
            ComposeColor(d),
        )
    }
}