package com.netDashboard.theme

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
import com.netDashboard.*
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.globals.G.mapper
import java.io.File
import java.io.FileReader

@Suppress("UNUSED")
class Theme {

    val a = Artist()

    fun apply(context: Context, viewGroup: ViewGroup) {
        context.setTheme(if (!a.isDark) R.style.Theme_Dark else R.style.Theme_Light)

        try {
            WindowInsetsControllerCompat(
                (context as Activity).window,
                viewGroup
            ).isAppearanceLightStatusBars = !a.isDark

            context.window.statusBarColor = a.colorBackground
        } catch (e: Exception) {

        }

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
            is RadioButton -> this.applyTheme()
            is MaterialButton -> this.applyTheme()
            is SwitchMaterial -> this.applyTheme()
            is EditText -> this.applyTheme()
            is TextView -> this.applyTheme()
            is Chip -> this.applyTheme()
            is Slider -> this.applyTheme()
            is LinearLayout -> this.applyTheme()
            is FrameLayout -> this.applyTheme()
            is ConstraintLayout -> this.applyTheme()
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
            "color" -> this.setBackgroundColor(a.color)
            "colorA" -> this.setBackgroundColor(a.colorA)
            "colorB" -> this.setBackgroundColor(a.colorB)
            "colorC" -> this.setBackgroundColor(a.colorC)
            "colorD" -> this.setBackgroundColor(a.colorD)
            "background" -> this.setBackgroundColor(a.colorBackground)
            "sliderBackground" -> {
                val drawable = GradientDrawable()
                drawable.mutate()
                drawable.setColor(a.colorD)
                drawable.cornerRadius = 15f
                this.background = drawable
            }
            "colorIcon" -> this.backgroundTintList = ColorStateList.valueOf(a.color)
            "groupArrow" -> this.backgroundTintList = ColorStateList.valueOf(a.color)
            "bar" -> this.backgroundTintList = ColorStateList.valueOf(contrastColor(!a.isDark, 200))
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, a.color)
            }
            "sliderPopupFrame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(10, a.color)
                drawable?.cornerRadius = 25f
            }
            "rippleForeground" -> {
                val background = this.background as RippleDrawable
                background.setColor(ColorStateList.valueOf(a.colorBackground.alpha(150)))
            }
            else -> onUnknownTag(this.tag, "view")
        }
    }

    private fun FrameLayout.applyTheme() {
        when (this.tag) {
            "background" -> this.setBackgroundColor(a.colorBackground)
            else -> onUnknownTag(this.tag, "frameLayout")
        }

        //if (this.tag != "item" && context !is DashboardActivity) {
        //    this.layoutTransition = LayoutTransition()
        //    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //}
    }

    private fun ConstraintLayout.applyTheme() {
        when (this.tag) {
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(6, a.color)
                drawable?.cornerRadius = 15f
            }
            else -> onUnknownTag(this.tag, "constraintLayout")
        }

        //if (this.tag != "item" && context !is DashboardActivity) {
        //    this.layoutTransition = LayoutTransition()
        //    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //}
    }

    private fun LinearLayout.applyTheme() {
        when (this.tag) {
            "background" -> this.setBackgroundColor(a.colorBackground)
            "frame" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.mutate()
                drawable?.setStroke(1, a.color)
            }
            "groupBar" -> this.setBackgroundColor(a.colorBackground.contrast(!a.isDark, 0.3f))
            "group" -> this.setBackgroundColor(a.colorBackground.contrast(!a.isDark, 0.1f))
            else -> onUnknownTag(this.tag, "linearLayout")
        }

        //if (context !is DashboardActivity) {
        //    this.layoutTransition = LayoutTransition()
        //    this.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        //}
    }

    private fun RecyclerView.applyTheme() {
        when (this.tag) {
            "log" -> {
                val drawable = this.background as? GradientDrawable
                drawable?.setStroke(1, a.color)
            }
            else -> onUnknownTag(this.tag, "recyclerView")
        }
    }

    private fun MaterialButton.applyTheme() {
        val background = this.background as LayerDrawable?
        val ripple = background?.findDrawableByLayerId(R.id.ripple) as RippleDrawable?

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
                "color" -> a.colorBackground
                "colorA" -> blendARGB(a.color, a.colorBackground, 0.9f)
                "colorB" -> blendARGB(a.color, a.colorBackground, 0.1f)
                "colorC" -> a.color
                else -> a.color
            }
        )

        this.backgroundTintList = when (this.tag) {
            "color" -> ColorStateList.valueOf(a.color)
            "colorA" -> ColorStateList.valueOf(a.colorA)
            "colorB" -> ColorStateList.valueOf(a.colorB)
            "colorC" -> ColorStateList.valueOf(a.colorC)
            else -> ColorStateList.valueOf(a.colorC)
        }
    }

    private fun RadioButton.applyTheme() {

        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked),
                intArrayOf(android.R.attr.state_checked)
            ), intArrayOf(
                a.colorC, a.colorB
            )
        )

        when (this.tag) {
            "default" -> {
                this.setTextColor(colorStateList)
                this.buttonTintList = colorStateList
            }
            else -> onUnknownTag(this.tag, "radioButton")
        }
    }

    private fun TextView.applyTheme() {
        when (this.tag) {
            "color" -> this.setTextColor(a.color)
            "colorA" -> this.setTextColor(a.colorA)
            "colorB" -> this.setTextColor(a.colorB)
            "colorC" -> this.setTextColor(a.colorC)
            "colorD" -> this.setTextColor(a.colorD)
            "colorBackground" -> this.setTextColor(a.colorBackground)
            "tag" -> {
                this.setTextColor(a.color)
                this.setBackgroundColor(a.colorD)
            }
            "log" -> {
                this.setTextColor(a.color)
                this.setBackgroundColor(a.colorBackground)
            }
            else -> onUnknownTag(this.tag, "textView")
        }
    }

    private fun EditText.applyTheme() {
        when (this.tag) {
            "basic" -> {
                this.setTextColor(a.colorB)
                this.setHintTextColor(a.colorC)
                this.setBackgroundColor(a.colorBackground.contrast(!a.isDark, 0.2f))
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
            a.colorC,
            a.colorB
        )

        val list = ColorStateList(states, colors)
        this.trackTintList = list
        this.thumbTintList =
            ColorStateList.valueOf(
                a.color
            )
    }

    private fun Slider.applyTheme() {
        when (this.tag) {
            "enabled" -> {
                this.trackActiveTintList = ColorStateList.valueOf(a.colorB)
                this.tickActiveTintList = ColorStateList.valueOf(a.colorB)
                this.trackInactiveTintList = ColorStateList.valueOf(a.colorC)
                this.tickInactiveTintList = ColorStateList.valueOf(a.colorC)
                this.thumbTintList = ColorStateList.valueOf(a.color)
            }
            "disabled" -> {
                this.trackActiveTintList = ColorStateList.valueOf(a.colorC)
                this.tickActiveTintList = ColorStateList.valueOf(a.colorC)
                this.trackInactiveTintList = ColorStateList.valueOf(a.colorD)
                this.tickInactiveTintList = ColorStateList.valueOf(a.colorD)
                this.thumbTintList = ColorStateList.valueOf(a.colorB)
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

    private fun enableTransitions() {

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

        var isDark = false
            set(value) {
                field = value
                compute()
            }

        var color = 0
            private set
        var colorA = 0
            private set
        var colorB = 0
            private set
        var colorC = 0
            private set
        var colorD = 0
            private set
        var colorBackground = 0
            private set

        private var maxS: Float = 1f
        private var maxV: Float = 1f
        private var minV: Float = 0f

        var hsv: FloatArray = floatArrayOf(174f, 1f, 1f)
            set(value) {
                field = value
                compute()
            }

        init {
            compute()
        }

        fun compute() {
            var c = 0

            //Compute maximal saturation/value
            for (i in 100 downTo 0) {
                c = Color.HSVToColor(
                    floatArrayOf(
                        hsv[0],
                        if (isDark) i / 100f else hsv[1],
                        if (isDark) 1f else i / 100f
                    )
                )

                if (ColorUtils.calculateContrast(c, getBackgroundColor(c)) > 2.6) {
                    (i * 0.008f).let {
                        maxS = if (isDark) it else 1f
                        maxV = if (isDark) 1f else it
                    }
                    break
                }
            }

            //Compute minimal value
            if (!isDark) minV = 0f
            else {
                for (i in 100 downTo 0) {
                    if (ColorUtils.calculateContrast(c, getBackgroundColor(c)) < 3.6) {
                        minV = i / 100f
                        break
                    }

                    c = Color.HSVToColor(floatArrayOf(hsv[0], hsv[1], i / 100f))
                }
            }

            color = Color.HSVToColor(
                floatArrayOf(
                    hsv[0],
                    maxS * hsv[1],
                    minV + (maxV - minV) * hsv[2]
                )
            )

            colorBackground = getBackgroundColor(color)

            colorA = blendARGB(color, colorBackground, 0.4f)
            colorB = blendARGB(color, colorBackground, 0.6f)
            colorC = blendARGB(color, colorBackground, 0.8f)
            colorD = blendARGB(color, colorBackground, 0.9f)
        }

        private fun getBackgroundColor(color: Int): Int {
            val hsv = floatArrayOf(0f, 0f, 0f)
            Color.colorToHSV(color, hsv)
            hsv[1] = hsv[1] * 0.1f

            return Color.HSVToColor(hsv).contrast(!isDark, 0.6f)
        }
    }
}