package com.netDashboard.activities.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.databinding.DashboardSettingsActivityBinding

class MainSettingsActivity : AppCompatActivity() {

    private lateinit var b: DashboardSettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardSettingsActivityBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}