package com.netDashboard.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard_new.DashboardNewActivity
import com.netDashboard.activities.settings.SettingsActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.foreground_service.ForegroundService.Companion.service
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.settings
import com.netDashboard.screenHeight

class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding

    private lateinit var adapter: DashboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)
        setContentView(b.root)

        setupRecyclerView()

        b.mTouch.setOnClickListener {
            touchOnClick()
        }

        b.mSettings.setOnClickListener {
            settingsOnClick()
        }

        b.mEdit.setOnClickListener {
            editOnClick()
        }

        b.mSwap.setOnClickListener {
            swapOnClick()
        }

        b.mRemove.setOnClickListener {
            removeOnClick()
        }

        b.mAdd.setOnClickListener {
            addOnClick()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onBackPressed() {
        if (!adapter.editType.isNone) {
            b.mTouch.callOnClick()
        } else super.onBackPressed()
    }

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(this)
        adapter.setHasStableIds(true)

        adapter.onItemRemove = {
            if (adapter.itemCount == 0) {
                b.mPlaceholder.visibility = View.VISIBLE
            }

            it.isDeprecated = true
            service?.dgc?.notifyDashboardRemoved(it)
        }

        adapter.onItemClick = { item ->
            if (adapter.editType.isEdit) {
                Intent(this, DashboardPropertiesActivity::class.java).also {
                    it.putExtra("dashboardId", item.id)
                    it.putExtra("exitActivity", "MainActivity")
                    startActivity(it)
                }
            } else if (adapter.editType.isNone) {
                Intent(this, DashboardActivity::class.java).also {
                    settings.lastDashboardId = item.id

                    it.putExtra("dashboardId", item.id)
                    startActivity(it)
                }
            }
        }

        adapter.submitList(dashboards)

        val layoutManager = GridLayoutManager(this, 1)

        b.mRecyclerView.layoutManager = layoutManager
        b.mRecyclerView.adapter = adapter


        if (adapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }
    }

    private fun touchOnClick() {
        if (adapter.editType.isNone) {
            adapter.editType.setEdit()
            highlightOnly(b.mEdit)

            b.mBar.visibility = View.VISIBLE
            b.mBar.y = screenHeight.toFloat()
            b.mBar.animate()
                .translationY(0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        } else {
            adapter.editType.setNone()

            b.mBar.animate()
                ?.y(screenHeight.toFloat())
                ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        }
    }

    private fun settingsOnClick() {
        Intent(this, SettingsActivity::class.java).also {
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.mEdit)
        adapter.editType.setEdit()
    }

    //----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.mSwap)
        adapter.editType.setSwap()
    }

    //----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun removeOnClick() {
        if (adapter.editType.isNone) return

        highlightOnly(b.mRemove)

        if (!adapter.editType.isRemove) {
            adapter.editType.setRemove()
        } else {
            adapter.removeMarkedItem()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun addOnClick() {
        Intent(this, DashboardNewActivity::class.java).also {
            startActivity(it)
        }
    }

    private fun onServiceReady() {}

    private fun highlightOnly(button: Button) {
        b.mRemove.alpha = 0.4f
        b.mSwap.alpha = 0.4f
        b.mEdit.alpha = 0.4f
        button.alpha = 1f
    }

    @Suppress("UNUSED_PARAMETER")
    fun clickTouch(v: View) = b.mTouch.click()

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

    private fun Button.click() {
        this.performClick()
        this.isPressed = true
        this.invalidate()
        this.isPressed = false
        this.invalidate()
    }
}
