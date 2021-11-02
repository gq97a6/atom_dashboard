package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard_new.DashboardNewActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.blink
import com.netDashboard.click
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.toolbarControl.toolBarControl

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private lateinit var adapter: DashboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupRecyclerView()
        G.theme.apply(this, b.root)

        val addOnClick: () -> Unit = {
            Intent(this, DashboardNewActivity::class.java).also {
                startActivity(it)
            }
        }

        val onUiChange: (vg: ViewGroup) -> Unit = { vg ->
            G.theme.apply(this, vg)
        }

        toolBarControl(
            adapter,
            b.mBar,
            b.mLock,
            b.mEdit,
            b.mSwap,
            b.mRemove,
            b.mAdd,
            addOnClick,
            onUiChange
        )

        b.mSettings.setOnClickListener {
            settingsOnClick()
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
        if (!adapter.editMode.isNone) {
            b.mLock.callOnClick()
        } else finishAffinity()
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(this)
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.mPlaceholder.visibility = View.VISIBLE
            b.mRemove.clearAnimation()

            service?.dgc?.notifyDashboardRemoved(it)
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.mRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.mRemove.clearAnimation()
        }

        adapter.onItemClick = { item ->
            if (adapter.editMode.isEdit) {
                Intent(this, DashboardPropertiesActivity::class.java).also {
                    it.putExtra("dashboardId", item.id)
                    it.putExtra("exitActivity", "MainActivity")
                    startActivity(it)
                }
            } else if (adapter.editMode.isNone) {
                Intent(this, DashboardActivity::class.java).also {
                    settings.lastDashboardId = item.id

                    it.putExtra("dashboardId", item.id)
                    startActivity(it)
                }
            }
        }

        adapter.submitList(dashboards)

        val layoutManager = LinearLayoutManager(this)

        b.mRecyclerView.layoutManager = layoutManager
        b.mRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun settingsOnClick() {
        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    fun clickLock(v: View) = b.mLock.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickSettings(v: View) = b.mSettings.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickAdd(v: View) = b.mAdd.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickRemove(v: View) = b.mRemove.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickSwap(v: View) = b.mSwap.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickEdit(v: View) = b.mEdit.click()
}
