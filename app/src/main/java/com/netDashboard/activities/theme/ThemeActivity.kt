package com.netDashboard.activities.theme

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.databinding.ActivityThemeBinding
import com.netDashboard.themes.Theme

class ThemeActivity : AppCompatActivity() {
    private lateinit var b: ActivityThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityThemeBinding.inflate(layoutInflater)
        viewConfig()
        Theme.apply(this, b.root)
        setContentView(b.root)

        b.tHue.addOnChangeListener { _, _, _ ->
            Theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            Theme.apply(this, b.root)
        }

        b.tSaturation.addOnChangeListener { _, _, _ ->
            Theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            Theme.apply(this, b.root)
        }

        b.tValue.addOnChangeListener { _, _, _ ->
            Theme.color = Color.HSVToColor(
                floatArrayOf(
                    b.tHue.value,
                    1 - b.tSaturation.value,
                    1 - b.tValue.value
                )
            )
            Theme.apply(this, b.root)
        }

        b.tIsDark.setOnCheckedChangeListener { _, state ->
            Theme.isDark = state
            Theme.apply(this, b.root)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun viewConfig() {
        val hsv = floatArrayOf(0f, 0f, 0f)
        Color.colorToHSV(Theme.color, hsv)

        b.tHue.value = hsv[0]
        b.tSaturation.value = 1 - hsv[1]
        b.tValue.value = 1 - hsv[2]

        b.tIsDark.isChecked = Theme.isDark
    }
}