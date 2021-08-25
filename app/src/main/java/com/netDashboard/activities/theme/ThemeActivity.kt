package com.netDashboard.activities.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivityThemeBinding
import com.netDashboard.globals.G

class ThemeActivity : AppCompatActivity() {
    private lateinit var b: ActivityThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityThemeBinding.inflate(layoutInflater)
        viewConfig()
        G.theme.apply(this, b.root)
        setContentView(b.root)

        b.tHue.addOnChangeListener { _, _, _ ->
            G.theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            G.theme.apply(this, b.root)
        }

        b.tSaturation.addOnChangeListener { _, _, _ ->
            G.theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            G.theme.apply(this, b.root)
        }

        b.tValue.addOnChangeListener { _, _, _ ->
            G.theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            G.theme.apply(this, b.root)
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            G.theme.isDark = state
            G.theme.apply(this, b.root)
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

        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun viewConfig() {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(G.theme.color, hsv)

        b.tHue.value = hsv[0]
        b.tSaturation.value = 1 - hsv[1]
        b.tValue.value = 1 - hsv[2]

        b.tIsDark.isChecked = G.theme.isDark
    }
}