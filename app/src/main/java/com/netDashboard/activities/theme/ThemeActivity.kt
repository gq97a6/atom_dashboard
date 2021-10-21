package com.netDashboard.activities.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
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

    private var h = 0f
    private var s = 0f
    private var v = 0f

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
            if (e.action == ACTION_DOWN) h = b.tHue.value

            if (theme.isAutoAdjust) setAdjustedColor()

            theme.color = getColor()
            theme.apply(this, b.root)

            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tSaturation.setOnTouchListener { _, e ->
            if (e.action == ACTION_DOWN) s = b.tSaturation.value

            if (e.action == ACTION_UP && con < 1.3) {
                b.tSaturation.value = s
                runConAlert()
            }

            theme.color = getColor()
            theme.apply(this, b.root)

            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tValue.setOnTouchListener { _, e ->
            if (e.action == ACTION_DOWN) v = b.tValue.value

            if (e.action == ACTION_UP && con < 1.3) {
                b.tValue.value = v
                runConAlert()
            }

            theme.color = getColor()
            theme.apply(this, b.root)

            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.isDark = state

            if (theme.isAutoAdjust) setAdjustedColor()
            else theme.color = getColor()

            if (con < 1.4) {
                setAdjustedColor()
                theme.apply(this, b.root)
                runConAlert()
            }

            theme.apply(this, b.root)
        }

        b.tAuto.setOnCheckedChangeListener { _, state ->
            theme.isAutoAdjust = state

            if (theme.isAutoAdjust) setAdjustedColor()
            else theme.color = getColor()

            theme.apply(this, b.root)
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

    private fun viewConfig() {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(theme.color, hsv)

        b.tHue.value = hsv[0]
        b.tSaturation.value = hsv[1]
        b.tValue.value = hsv[2]
        b.tAuto.isChecked = theme.isAutoAdjust
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

    private fun getColor(): Int {
        return Color.HSVToColor(
            floatArrayOf(
                b.tHue.value,
                b.tSaturation.value,
                b.tValue.value
            )
        )
    }

    private fun runConAlert() {
        b.tConWarning.clearAnimation()
        b.tConWarning.startAnimation(AlphaAnimation(1f, 0f).also {
            it.duration = 200
            it.startOffset = 50
            it.repeatMode = Animation.REVERSE
            it.repeatCount = 6
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(a: Animation?) {
                    b.tConWarning.visibility = VISIBLE
                }

                override fun onAnimationEnd(a: Animation?) {
                    b.tConWarning.visibility = GONE
                }

                override fun onAnimationRepeat(a: Animation?) {}
            })
        })
    }

    private fun setAdjustedColor() {

        b.tValue.value = 1f
        b.tSaturation.value = 1f

        theme.color = getColor()

        if (theme.isDark) {
            for (i in 100 downTo 0) {

                if (con > 2.6) {
                    b.tSaturation.value = i * 0.008f
                    break
                }

                theme.color = Color.HSVToColor(
                    floatArrayOf(
                        b.tHue.value,
                        i / 100f,
                        b.tValue.value
                    )
                )
            }
        } else {
            for (i in 100 downTo 0) {

                if (con > 2.6) {
                    b.tValue.value = i * 0.007f
                    break
                }

                theme.color = Color.HSVToColor(
                    floatArrayOf(
                        b.tHue.value,
                        b.tSaturation.value,
                        i / 100f,
                    )
                )
            }
        }
    }
}