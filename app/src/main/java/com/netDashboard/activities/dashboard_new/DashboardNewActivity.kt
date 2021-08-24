package com.netDashboard.activities.dashboard_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityDashboardNewBinding
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.themes.Theme
import kotlin.random.Random

class DashboardNewActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDashboardNewBinding.inflate(layoutInflater)
        Theme.apply(this, b.root)
        setContentView(b.root)

        val name = kotlin.math.abs(Random.nextInt()).toString()
        val dashboard = Dashboard(name)
        Dashboards.add(dashboard)

        service?.dgc?.notifyDashboardAdded(dashboard)

        Intent(this, DashboardPropertiesActivity::class.java).also {
            it.putExtra("dashboardId", dashboard.id)
            it.putExtra("exitActivity", "MainActivity")
            startActivity(it)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    override fun onBackPressed() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
    }
}