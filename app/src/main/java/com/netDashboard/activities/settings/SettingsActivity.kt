package com.netDashboard.activities.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}