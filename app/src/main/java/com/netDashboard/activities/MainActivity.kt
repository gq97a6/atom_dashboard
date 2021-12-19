package com.netDashboard.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.R
import com.netDashboard.activities.fragments.MainScreenFragment
import com.netDashboard.activities.fragments.dashboard.DashboardFragment
import com.netDashboard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.m_fragment, MainScreenFragment())
            commit()
        }
    }
}
