package com.netDashboard.activities.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.databinding.ActivityDashboardBinding

@SuppressLint("ClickableViewAccessibility")
class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AppOn.create(this)

        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}