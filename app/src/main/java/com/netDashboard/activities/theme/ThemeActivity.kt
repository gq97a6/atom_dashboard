package com.netDashboard.activities.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent.ACTION_UP
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivityThemeBinding
import com.netDashboard.globals.G
import com.netDashboard.theme.Theme

class ThemeActivity : AppCompatActivity() {
    private lateinit var b: ActivityThemeBinding

    private lateinit var exitActivity: String
    private var dashboardId: Long = 0
    private lateinit var theme: Theme

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        exitActivity = intent.getStringExtra("exitActivity") ?: ""
        theme = G.theme

        b = ActivityThemeBinding.inflate(layoutInflater)
        viewConfig()
        theme.apply(this, b.root)
        setContentView(b.root)

        fun onColorChange() {
            theme.a.hsv = floatArrayOf(b.tHue.value, b.tSaturation.value, b.tValue.value)
            theme.apply(this, b.root)
        }

        b.tHue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tSaturation.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tValue.setOnTouchListener { _, e ->
            onColorChange()
            return@setOnTouchListener e.action == ACTION_UP
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.a.isDark = state

            b.tValText.tag = if (state) "colorC" else "colorB"
            b.tValue.tag = if (state) "disabled" else "enabled"
            b.tValue.isEnabled = !state
            if (state) b.tValue.value = 1f

            theme.a.compute()
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
        b.tHue.value = theme.a.hsv[0]
        b.tSaturation.value = theme.a.hsv[1]
        b.tValue.value = theme.a.hsv[2]

        b.tValText.tag = if (theme.a.isDark) "colorC" else "colorB"
        b.tValue.tag = if (theme.a.isDark) "disabled" else "enabled"
        b.tValue.isEnabled = !theme.a.isDark
        if (theme.a.isDark) b.tValue.value = 1f

        if (b.tSaturation.value + b.tValue.value < 2) {
            b.tAdvancedArrow.rotation = 0f
            b.tAdvanced.visibility = VISIBLE
        }

        b.tIsDark.isChecked = theme.a.isDark
    }

    private fun switchAdvancedTab() {
        b.tAdvanced.let {
            it.visibility = if (it.isVisible) GONE else VISIBLE
            b.tAdvancedArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
}