package com.netDashboard.activities.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.theme.ThemeActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivitySettingsBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.settings

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        b = ActivitySettingsBinding.inflate(layoutInflater)
        viewConfig()
        G.theme.apply(this, b.root)
        setContentView(b.root)

        b.sLast.setOnCheckedChangeListener { _, state ->
            settings.startFromLast = state
        }

        b.sThemeEdit.setOnClickListener {
            Intent(this, ThemeActivity::class.java).also {
                it.putExtra("exitActivity", "SettingsActivity")
                startActivity(it)
            }
        }

        b.sThemeIsDark.setOnCheckedChangeListener { _, state ->
            G.theme.a.isDark = state
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

        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun viewConfig() {
        b.sLast.isChecked = settings.startFromLast
        b.sThemeIsDark.isChecked = G.theme.a.isDark
    }
}