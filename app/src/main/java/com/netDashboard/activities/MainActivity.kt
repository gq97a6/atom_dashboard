package com.netDashboard.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.Activity
import com.netDashboard.activities.fragments.DashboardFragment
import com.netDashboard.activities.fragments.MainScreenFragment
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.globals.G.settings
import com.netDashboard.switchTo

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(this, b.root)

        supportFragmentManager.switchTo(MainScreenFragment(), false)
        if (settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
            supportFragmentManager.switchTo(DashboardFragment())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Activity.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Activity.onPause()
        finish()
    }
}
