package com.netDashboard.activities.dashboard

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.netDashboard.*
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
import com.netDashboard.log.LogAdapter
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toolbarControl.ToolBarController
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    private lateinit var dashboard: Dashboard
    private lateinit var adapter: TilesAdapter
    private lateinit var toolBarController: ToolBarController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        dashboard = dashboards.byId(intent.getLongExtra("dashboardId", 0))
        if (dashboard.isInvalid) Intent(this, MainActivity::class.java).also {
            startActivity(it)
        }

        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupRecyclerView()
        setupLogRecyclerView()
        G.theme.apply(this, b.root)

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Set dashboard status
        dashboard.dg?.mqttd?.let {
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

        if (!adapter.editMode.isNone) {
            b.dLock.setBackgroundResource(R.drawable.button_unlocked)
            b.dBar.translationY = 0f

            adapter.editMode.let {
                highlightOnly(
                    when {
                        it.isRemove -> b.dRemove
                        it.isSwap -> b.dSwap
                        it.isEdit -> b.dEdit
                        else -> b.dEdit
                    }
                )
            }
        }

        fun updateTilesStatus() {
            for (tile in adapter.list) {
                tile.holder?.itemView?.findViewById<TextView>(R.id.t_status)?.let {
                    tile.mqttData.lastReceive.time.let { lr ->
                        val t = Date().time - lr
                        if (lr != 0L) it.text = (t / 1000).let { s ->
                            if (s < 60) if (s == 1L) "$s second ago" else "$s seconds ago"
                            else (t / 60000).let { m ->
                                if (m < 60) if (m == 1L) "$m minute ago" else "$m minutes ago"
                                else (t / 3600000).let { h ->
                                    if (h < 24) if (h == 1L) "$h hour ago" else "$h hours ago"
                                    else (t / 86400000).let { d ->
                                        if (d < 365) if (d == 1L) "$d day ago" else "$d days ago"
                                        else (t / 31536000000).let { y ->
                                            if (y == 1L) "$y year ago" else "$y years ago"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).postDelayed({
                updateTilesStatus()
            }, 500)
        }
        updateTilesStatus()

        val addOnClick: () -> Unit = {
            Intent(this, TileNewActivity::class.java).also {
                it.putExtra("dashboardId", dashboard.id)
                startActivity(it)
            }
        }

        val onUiChange: (vg: ViewGroup) -> Unit = { vg ->
            G.theme.apply(this, vg)
        }

        toolBarController = ToolBarController(
            adapter,
            b.dBar,
            b.dLock,
            b.dEdit,
            b.dSwap,
            b.dRemove,
            b.dAdd,
            addOnClick,
            onUiChange
        )

        b.dProperties.setOnClickListener {
            propertiesOnClick()
        }

        b.dTag.setOnTouchListener { v, e ->
            showLog(v, e)

            return@setOnTouchListener true
        }

        b.dTagStatus.setOnTouchListener { v, e ->
            showLog(v, e)

            return@setOnTouchListener true
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

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
        if (!adapter.editMode.isNone) {
            b.dLock.callOnClick()
        } else {
            super.onBackPressed()

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = 2

        adapter = TilesAdapter(this, spanCount)
        adapter.setHasStableIds(true)
        adapter.theme = G.theme

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
            b.dRemove.clearAnimation()
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.dRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.dRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            Intent(this, TilePropertiesActivity::class.java).also {
                it.putExtra("tileIndex", adapter.list.indexOf(item))
                it.putExtra("dashboardId", dashboard.id)
                startActivity(it)
            }
        }

        adapter.onItemLongClick = {
            toolBarController.toggleTools()
        }

        adapter.submitList(dashboard.tiles.toMutableList())

        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        //layoutManager.spanSizeLookup =
        //    object : GridLayoutManager.SpanSizeLookup() {
        //        override fun getSpanSize(position: Int): Int {
        //            return adapter.list[position].width
        //        }
        //    }

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter

        adapter.editMode.setNone()
        dashboard.tilesAdapterEditMode?.let {
            adapter.editMode = it
        }

        dashboard.tilesAdapterEditMode = adapter.editMode

        if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
    }

    private fun setupLogRecyclerView() {
        val adapter = LogAdapter(this)
        adapter.setHasStableIds(true)
        adapter.theme = G.theme
        adapter.submitList(dashboard.log.list)

        val layoutManager = LinearLayoutManager(this)

        layoutManager.stackFromEnd = true
        b.dLogRecyclerView.layoutManager = layoutManager
        b.dLogRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.dLogPlaceholder.visibility = View.VISIBLE
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

    private fun highlightOnly(button: Button) {
        b.dRemove.alpha = 0.4f
        b.dSwap.alpha = 0.4f
        b.dEdit.alpha = 0.4f
        button.alpha = 1f
    }

    private var showLogStartY = 0f
    private fun showLog(v: View, e: MotionEvent) {
        v.performClick()

        if (e.action == ACTION_DOWN) showLogStartY = e.rawY

        if (e.action == ACTION_UP) {
            val valueAnimator = ValueAnimator.ofInt(b.dLog.measuredHeight, 0)
            valueAnimator.duration = 500L

            valueAnimator.addUpdateListener {
                val animatedValue = valueAnimator.animatedValue as Int
                val layoutParams = b.dLog.layoutParams
                layoutParams.height = animatedValue
                b.dLog.layoutParams = layoutParams
            }
            valueAnimator.start()
        } else {
            val lp = b.dLog.layoutParams
            lp.height = (e.rawY - showLogStartY).toInt().let {
                when {
                    it <= 0 -> 0
                    it <= (0.9 * screenHeight) -> it
                    else -> {
                        dashboard.log.flush()
                        (0.9 * screenHeight).toInt()
                    }
                }
            }

            b.dLog.layoutParams = lp
        }
    }

//----------------------------------------------------------------------------------------------

    @Suppress("UNUSED_PARAMETER")
    fun clickLock(v: View) = b.dLock.click()

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
}