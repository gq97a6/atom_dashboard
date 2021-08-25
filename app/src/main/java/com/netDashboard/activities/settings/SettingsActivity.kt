package com.netDashboard.activities.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.theme.ThemeActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.ActivitySettingsBinding
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Theme

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySettingsBinding.inflate(layoutInflater)
        viewConfig()
        Theme.apply(this, b.root)
        setContentView(b.root)

        b.sLast.setOnCheckedChangeListener { _, state ->
            Settings.startFromLast = state
        }

        b.sThemeEdit.setOnClickListener {
            Intent(this, ThemeActivity::class.java).also {
                startActivity(it)
            }
        }

        b.sThemeIsGlobal.setOnCheckedChangeListener { _, state ->
            Theme.isGlobal = state
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun viewConfig() {
        b.sLast.isChecked = Settings.startFromLast
        b.sThemeIsGlobal.isChecked = Theme.isGlobal
    }
}