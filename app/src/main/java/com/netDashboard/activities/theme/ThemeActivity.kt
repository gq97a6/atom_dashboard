package com.netDashboard.activities.theme

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.databinding.ActivityThemeBinding
import com.netDashboard.themes.Theme

class ThemeActivity : AppCompatActivity() {
    private lateinit var b: ActivityThemeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityThemeBinding.inflate(layoutInflater)
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
}