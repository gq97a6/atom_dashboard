package com.netDashboard.activities.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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

        b.tHue.addOnChangeListener { _, _, _ ->
            theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            theme.apply(this, b.root)
        }

        b.tSaturation.addOnChangeListener { _, _, _ ->
            theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            theme.apply(this, b.root)
        }

        b.tValue.addOnChangeListener { _, _, _ ->
            theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            theme.apply(this, b.root)
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            theme.isDark = state
            theme.apply(this, b.root)
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
        b.tSaturation.value = 1 - hsv[1]
        b.tValue.value = 1 - hsv[2]

        b.tIsDark.isChecked = theme.isDark
    }
}