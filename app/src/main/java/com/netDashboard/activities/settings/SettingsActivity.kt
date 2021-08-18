package com.netDashboard.activities.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.databinding.ActivitySettingsBinding
import com.netDashboard.settings.Settings
import com.netDashboard.themes.Themes

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySettingsBinding.inflate(layoutInflater)
        Themes.applyTheme(this, b.root)
        setContentView(b.root)

        b.sLast.setOnCheckedChangeListener { _, state ->
            Settings.startFromLast = state
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun viewConfig() {

        b.sLast.isChecked = Settings.startFromLast

    }
}