package com.netDashboard.activities.dashboard

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.*
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard.tile_new.TileNewActivity
import com.netDashboard.activities.dashboard.tile_properties.TilePropertiesActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityDashboardBinding
import com.netDashboard.globals.G.dashboards
import com.netDashboard.log.Log.Companion.LogList
import com.netDashboard.log.LogAdapter
import com.netDashboard.tile.TilesAdapter
import java.util.*


@SuppressLint("ClickableViewAccessibility")
class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    private lateinit var dashboard: Dashboard
    lateinit var adapter: TilesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        dashboard = dashboards.byId(intent.getLongExtra("dashboardId", 0))

        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupRecyclerView()
        setupLogRecyclerView()
        dashboard.resultTheme.apply(this, b.root)

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

        if (!adapter.editMode.isNone) {

            b.dTouch.setBackgroundResource(R.drawable.button_unlocked)
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
            b.dTouch.callOnClick()
        } else {
            super.onBackPressed()

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = dashboard.spanCount

        adapter = TilesAdapter(this, spanCount)
        adapter.setHasStableIds(true)
        adapter.theme = dashboard.resultTheme

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
            b.dRemove.clearAnimation()
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.dRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.dRemove.clearAnimation()
        }

        adapter.onItemEdit =
            { item ->
                Intent(this, TilePropertiesActivity::class.java).also {
                    it.putExtra("tileIndex", adapter.list.indexOf(item))
                    it.putExtra("dashboardId", dashboard.id)
                    startActivity(it)
                }
            }

        adapter.submitList(dashboard.tiles.toMutableList())

        val layoutManager = GridLayoutManager(this, spanCount)

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
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
        adapter.theme = dashboard.resultTheme
        adapter.submitList(LogList)

        val layoutManager = LinearLayoutManager(this)

        layoutManager.stackFromEnd = true
        b.dLogRecyclerView.layoutManager = layoutManager
        b.dLogRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.dLogPlaceholder.visibility = View.VISIBLE
        }
    }

//----------------------------------------------------------------------------------------------

    private fun touchOnClick() {
        if (adapter.editMode.isNone) {
            adapter.editMode.setEdit()
            editOnClick()

            b.dBar.animate()
                .translationY(0f)
                .withEndAction { b.dTouch.setBackgroundResource(R.drawable.button_unlocked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        } else {
            adapter.editMode.setNone()

            b.dBar.animate()
                .translationY(b.dBar.height.toFloat())
                .withEndAction { b.dTouch.setBackgroundResource(R.drawable.button_locked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
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
        if (adapter.editMode.isNone) return
        highlightOnly(b.dEdit)
        adapter.editMode.setEdit()
    }

//----------------------------------------------------------------------------------------------

    private fun swapOnClick() {
        if (adapter.editMode.isNone) return
        highlightOnly(b.dSwap)
        adapter.editMode.setSwap()
    }

//----------------------------------------------------------------------------------------------

    @SuppressLint("NotifyDataSetChanged")
    private fun removeOnClick(isLong: Boolean = false) {
        if (adapter.editMode.isNone) return

        if (!adapter.editMode.isRemove) {
            highlightOnly(b.dRemove)
            adapter.editMode.setRemove()
        } else {
            adapter.removeMarkedItems()
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
                    it <= (0.7 * screenHeight) -> it
                    else -> (0.7 * screenHeight).toInt()
                }
            }

            b.dLog.layoutParams = lp
        }
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
}