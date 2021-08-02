package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.settings.Settings

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private lateinit var dashboardAdapter: DashboardAdapter
    private lateinit var settings: Settings

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

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun setupRecyclerView() {

        dashboardAdapter = DashboardAdapter(this)
        dashboardAdapter.setHasStableIds(true)

        b.mRecyclerView.adapter = dashboardAdapter
        b.mRecyclerView.setItemViewCacheSize(20)

        val layoutManager = GridLayoutManager(this, 1)

        b.mRecyclerView.layoutManager = layoutManager
        //b.recyclerView.itemAnimator?.changeDuration = 0

        dashboardAdapter.submitList(Dashboards.get())

        if (dashboardAdapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }

        b.mSettings.setOnClickListener {
            settingsOnClick()
        }

        b.mTouch.setOnClickListener {
            touchOnClick()
        }
    }

    private fun touchOnClick() {
        //adapter.isEdit = !adapter.isEdit
//
        //if (!adapter.isEdit) {
        //    b.dRecyclerView.suppressLayout(false)
        //    dashboard.tiles = adapter.tiles
        //} else {
        //    highlightOnly(b.dEdit)
//
        //    adapter.editMode = true
//
        //    for (t in adapter.tiles) {
        //        t.isEdit = true
        //        t.flag()
        //    }
        //}
//
        //if (adapter.isEdit) {
        //    b.dBar.y = getScreenHeight().toFloat()
//
        //    b.dBar.visibility = View.VISIBLE
//
        //    b.dBar.animate()
        //        .translationY(0f)
        //        .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        //} else {
//
        //    b.dBar.animate()
        //        ?.y(getScreenHeight().toFloat())
        //        ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        //}
    }

    private fun settingsOnClick() {
        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun onServiceReady() {}


    fun clickTouch(v: View) = b.mTouch.click()
    fun clickSettings(v: View) = b.mSettings.click()
    fun clickAdd(v: View) = b.mAdd.click()
    fun clickRemove(v: View) = b.mRemove.click()
    fun clickSwap(v: View) = b.mSwap.click()
    fun clickEdit(v: View) = b.mEdit.click()

    private fun Button.click() {
        this.performClick()
        this.isPressed = true
        this.invalidate()
        this.isPressed = false
        this.invalidate()
    }
}
