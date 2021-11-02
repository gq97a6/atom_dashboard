package com.netDashboard.activities.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils.calculateContrast
import androidx.core.view.isVisible
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityThemeBinding
import com.netDashboard.globals.G
import com.netDashboard.theme.Theme

class ThemeActivity : AppCompatActivity() {
    private lateinit var b: ActivityThemeBinding

    private lateinit var exitActivity: String
    private var dashboardId: Long = 0
    private lateinit var theme: Theme

    private val color
        get() = color()

    private val con
        get() = calculateContrast(theme.color, theme.colorBackground)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        exitActivity = intent.getStringExtra("exitActivity") ?: ""
        dashboardId = intent.getLongExtra("dashboardId", 0)
        theme = if (dashboardId != 0L) G.dashboards.byId(dashboardId).theme else G.theme

        b = ActivityThemeBinding.inflate(layoutInflater)
        viewConfig()
        theme.apply(this, b.root)
        setContentView(b.root)

        b.tHue.setOnTouchListener { _, e ->
            computeRanges()
            onColorChange()

            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tSaturation.setOnTouchListener { _, e ->
            computeRanges()
            onColorChange()

            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tValue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.isDark = state

            b.tValText.tag = if (state) "colorC" else "colorB"
            b.tValue.tag = if (state) "disabled" else "enabled"
            b.tValue.isEnabled = !state
            if (state) b.tValue.value = 1f

            computeRanges()
            onColorChange()
        }

        b.tAdvancedArrow.setOnClickListener {
            switchAdvancedTab()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(
            this,
            when (exitActivity) {
                "SettingsActivity" -> SettingsActivity::class.java
                "DashboardPropertiesActivity" -> {
                    DashboardPropertiesActivity::class.java
                }
                else -> MainActivity::class.java
            }
        ).also {
            it.putExtra("dashboardId", dashboardId)
            it.putExtra("exitActivity", "DashboardActivity")
            startActivity(it)
        }
    }

    private fun onColorChange() {
        theme.color = color
        theme.hsv = floatArrayOf(b.tHue.value, b.tSaturation.value, b.tValue.value)
        theme.apply(this, b.root)
    }

    private fun viewConfig() {
        b.tHue.value = theme.hsv[0]
        b.tSaturation.value = theme.hsv[1]
        b.tValue.value = theme.hsv[2]
        computeRanges()

        b.tValText.tag = if (theme.isDark) "colorC" else "colorB"
        b.tValue.tag = if (theme.isDark) "disabled" else "enabled"
        b.tValue.isEnabled = !theme.isDark
        if (theme.isDark) b.tValue.value = 1f

        if(b.tSaturation.value + b.tValue.value < 2) {
            b.tAdvancedArrow.rotation = 0f
            b.tAdvanced.visibility = VISIBLE
        }

        b.tIsDark.isChecked = theme.isDark
    }

    private fun switchAdvancedTab() {
        b.tAdvanced.let {
            it.visibility = if (it.isVisible) GONE else VISIBLE
            b.tAdvancedArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }

    private var maxS: Float = 1f
    private var maxV: Float = 1f
    private var minV: Float = 0f

    private fun color(): Int {
        return Color.HSVToColor(
            floatArrayOf(
                b.tHue.value,
                maxS * b.tSaturation.value,
                minV + (maxV - minV) * b.tValue.value
            )
        )
    }

    private fun computeRanges() {

        //Compute maximal saturation/value
        for (i in 100 downTo 0) {
            theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    if (theme.isDark) i / 100f else b.tSaturation.value,
                    if (theme.isDark) 1f else i / 100f
                )
            )

            if (con > 2.6) {
                (i * 0.008f).let {
                    maxS = if (theme.isDark) it else 1f
                    maxV = if (theme.isDark) 1f else it
                }
                break
            }
        }

        //Compute minimal value
        if (!theme.isDark) minV = 0f
        else {
            for (i in 100 downTo 0) {
                if (con < 3.6) {
                    minV = i / 100f
                    break
                }

                theme.color = Color.HSVToColor(
                    floatArrayOf(
                        b.tHue.value,
                        b.tSaturation.value,
                        i / 100f
                    )
                )
            }
        }
    }
}