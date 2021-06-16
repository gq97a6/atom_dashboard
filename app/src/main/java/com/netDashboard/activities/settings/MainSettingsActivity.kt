package com.netDashboard.activities.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.databinding.ActivityDashboardSettingsBinding

class MainSettingsActivity : AppCompatActivity() {

    private lateinit var b: ActivityDashboardSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDashboardSettingsBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}