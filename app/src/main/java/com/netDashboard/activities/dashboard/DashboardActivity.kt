package com.netDashboard.activities.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard.tile_new.TileNewActivity
import com.netDashboard.activities.dashboard.tile_properties.TilePropertiesActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityDashboardBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.screenHeight
import com.netDashboard.tile.TilesAdapter
import java.util.*


class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    private lateinit var dashboard: Dashboard

    lateinit var adapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.onCreate(this)

        b = ActivityDashboardBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)
        setContentView(b.root)

        dashboard = dashboards.byId(intent.getLongExtra("dashboardId", 0))

        setupRecyclerView()

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Set dashboard status
        dashboard.daemonGroup?.mqttd?.let {
            it.conHandler.isDone.observe(this) { isDone ->
                b.dTagStatus.text = getString(
                    if (!dashboard.mqttEnabled) {
                        R.string.d_disconnected
                    } else {
                        if (it.client.isConnected) {
                            R.string.d_connected
                        } else {
                            if (isDone) {
                                R.string.d_failed
                            } else {
                                R.string.d_attempting
                            }
                        }
                    }
                )
            }
        }

        b.dTouch.setOnClickListener {
            touchOnClick()
        }

        b.dProperties.setOnClickListener {
            propertiesOnClick()
        }

        b.dEdit.setOnClickListener {
            editOnClick()
        }

        b.dSwap.setOnClickListener {
            swapOnClick()
        }

        b.dRemove.setOnClickListener {
            removeOnClick()
        }

        b.dAdd.setOnClickListener {
            addOnClick()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        if (!adapter.editType.isNone) {
            b.dEdit.callOnClick()
        }

        adapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()

        dashboard.tiles = adapter.list
        AppOn.pause()
    }

    override fun onBackPressed() {
        if (!adapter.editType.isNone) {
            b.dTouch.callOnClick()
        } else {
            super.onBackPressed()

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun onServiceReady() {
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = dashboard.spanCount

        adapter = TilesAdapter(this, spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemRemove = {
            if (adapter.itemCount == 0) {
                b.dPlaceholder.visibility = View.VISIBLE
            }
        }

        adapter.onItemClick = { item ->
            if (adapter.editType.isEdit) {
                Intent(this, TilePropertiesActivity::class.java).also {
                    it.putExtra("tileIndex", adapter.list.indexOf(item))
                    it.putExtra("dashboardId", dashboard.id)
                    startActivity(it)
                }
            }
        }

        adapter.submitList(dashboard.tiles.toMutableList())

        val layoutManager = GridLayoutManager(this, spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val t = adapter.list[position]
                return when {
                    t.height != 1 || t.width > spanCount -> spanCount
                    else -> t.width
                }
            }
        }

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.dPlaceholder.visibility = View.VISIBLE
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun touchOnClick() {
        if (adapter.editType.isNone) {
            adapter.editType.setEdit()
            highlightOnly(b.dEdit)

            b.dBar.visibility = View.VISIBLE
            b.dBar.y = screenHeight.toFloat()
            b.dBar.animate()
                .translationY(0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        } else {
            adapter.editType.setNone()

            b.dBar.animate()
                ?.y(screenHeight.toFloat())
                ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration = 400
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun propertiesOnClick() {
        Intent(this, DashboardPropertiesActivity::class.java).also {
            it.putExtra("exitActivity", "DashboardActivity")
            it.putExtra("dashboardId", dashboard.id)
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun editOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.dEdit)
        adapter.editType.setEdit()
    }

    //----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (adapter.editType.isNone) return
        highlightOnly(b.dSwap)
        adapter.editType.setSwap()
    }

    //----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun removeOnClick() {
        if (adapter.editType.isNone) return

        highlightOnly(b.dRemove)

        if (!adapter.editType.isRemove) {
            adapter.editType.setRemove()
        } else {
            adapter.removeMarkedItem()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun addOnClick() {
        Intent(this, TileNewActivity::class.java).also {
            it.putExtra("dashboardId", dashboard.id)
            startActivity(it)
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun highlightOnly(button: Button) {
        b.dRemove.alpha = 0.4f
        b.dSwap.alpha = 0.4f
        b.dEdit.alpha = 0.4f
        button.alpha = 1f
    }

    @Suppress("UNUSED_PARAMETER")
    fun clickTouch(v: View) = b.dTouch.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickProperties(v: View) = b.dProperties.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickAdd(v: View) = b.dAdd.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickRemove(v: View) = b.dRemove.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickSwap(v: View) = b.dSwap.click()

    @Suppress("UNUSED_PARAMETER")
    fun clickEdit(v: View) = b.dEdit.click()

    private fun Button.click() {
        this.performClick()
        this.isPressed = true
        this.invalidate()
        this.isPressed = false
        this.invalidate()
    }
}