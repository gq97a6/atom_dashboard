package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.MainActivityBinding
import com.netDashboard.main.Dashboards
import com.netDashboard.main.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var b: MainActivityBinding

    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = MainActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        settings = Settings(filesDir.canonicalPath).getSaved()

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

        dashboardAdapter.submitList(Dashboards().get(filesDir.canonicalPath))

        if (dashboardAdapter.itemCount == 0) {
            b.placeholder.visibility = View.VISIBLE
        }
    }
}
