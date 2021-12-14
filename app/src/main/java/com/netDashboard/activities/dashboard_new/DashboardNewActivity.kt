package com.netDashboard.activities.dashboard_new

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.databinding.ActivityDashboardNewBinding
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import kotlin.random.Random

class DashboardNewActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardNewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        b = ActivityDashboardNewBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)
        setContentView(b.root)

        val name = kotlin.math.abs(Random.nextInt()).toString()
        val dashboard = Dashboard(name)
        dashboards.add(dashboard)
        dashboards.save()

        service?.dgManager?.notifyDashboardAdded(dashboard)

        Intent(this, DashboardPropertiesActivity::class.java).also {
            it.putExtra("dashboardId", dashboard.id)
            it.putExtra("exitActivity", "MainActivity")
            startActivity(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }

    override fun onBackPressed() {
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }
    }
}