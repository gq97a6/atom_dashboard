package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.MainActivityBinding
import com.netDashboard.dashboard.DashboardSavedList
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.main_settings.MainSettings

class MainActivity : AppCompatActivity() {
    private lateinit var b: MainActivityBinding

    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var settings: MainSettings

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = MainActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                foregroundService = s
                onServiceReady()
            }
        })

        settings = MainSettings(filesDir.canonicalPath).getSaved()

        if (settings.lastDashboardName != null) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", settings.lastDashboardName)
                overridePendingTransition(0, 0)
                startActivity(it)
            }
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        dashboardAdapter = DashboardAdapter(this)
        dashboardAdapter.setHasStableIds(true)

        b.recyclerView.adapter = dashboardAdapter
        b.recyclerView.setItemViewCacheSize(20)

        val layoutManager = GridLayoutManager(this, 1)

        b.recyclerView.layoutManager = layoutManager
        //b.recyclerView.itemAnimator?.changeDuration = 0

        dashboardAdapter.submitList(DashboardSavedList().get(filesDir.canonicalPath))

        if (dashboardAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
    }

    private fun onServiceReady() {
    }
}
