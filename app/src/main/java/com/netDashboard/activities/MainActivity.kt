package com.netDashboard.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.dashboard.DashboardSavedList
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.main_settings.MainSettings

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var settings: MainSettings

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
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

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        dashboardAdapter = DashboardAdapter(this)
        dashboardAdapter.setHasStableIds(true)

        b.mRecyclerView.adapter = dashboardAdapter
        b.mRecyclerView.setItemViewCacheSize(20)

        val layoutManager = GridLayoutManager(this, 1)

        b.mRecyclerView.layoutManager = layoutManager
        //b.recyclerView.itemAnimator?.changeDuration = 0

        dashboardAdapter.submitList(DashboardSavedList().get(filesDir.canonicalPath))

        if (dashboardAdapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun onServiceReady() {
    }
}
