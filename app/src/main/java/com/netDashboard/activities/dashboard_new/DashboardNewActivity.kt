package com.netDashboard.activities.dashboard_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.databinding.ActivityDashboardNewBinding

class DashboardNewActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDashboardNewBinding.inflate(layoutInflater)
        setContentView(b.root)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    override fun onBackPressed() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
}