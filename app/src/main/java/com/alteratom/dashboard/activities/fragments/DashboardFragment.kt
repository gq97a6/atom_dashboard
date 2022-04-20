package com.alteratom.dashboard.activities.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboardIndex
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.setCurrentDashboard
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.SplashScreenActivity
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.log.LogAdapter
import com.alteratom.dashboard.tile.TilesAdapter
import com.alteratom.databinding.FragmentDashboardBinding
import com.alteratom.tile.types.slider.SliderTile
import java.util.*
import kotlin.math.abs

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var b: FragmentDashboardBinding

    private lateinit var adapter: TilesAdapter
    private lateinit var toolBarController: ToolBarController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (dashboard.isInvalid) Intent(context, MainActivity::class.java).also {
            startActivity(it)
        }

        setupRecyclerView()
        setupLogRecyclerView()
        theme.apply(b.root, requireContext(), false)
        settings.lastDashboardId = dashboard.id

        (activity as MainActivity).onBackPressedBoolean = {
            if (!adapter.editMode.isNone) {
                toolBarController.toggleTools()
                true
            } else {
                false
            }
        }

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Ensure
        if (dashboard.dg == null) {
            if (service == null) {
                Intent(requireContext(), SplashScreenActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                service?.dgManager?.assign()
            }
        }

        //Set dashboard status
        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(viewLifecycleOwner) { isDone ->
                b.dSslStatus.visibility = GONE
                b.dStatus.text = if (!dashboard.mqtt.isEnabled) {
                    "DISCONNECTED"
                } else {
                    if (it.client.isConnected) {
                        if (dashboard.mqtt.ssl && !dashboard.mqtt.sslTrustAll)
                            b.dSslStatus.visibility = VISIBLE
                        "CONNECTED"
                    } else {
                        if (isDone) {
                            "FAILED TO CONNECT"
                        } else {
                            "ATTEMPTING"
                        }
                    }
                }
            }
        }

        fun updateTilesStatus() {
            for (tile in adapter.list) {
                tile.holder?.itemView?.findViewById<TextView>(R.id.t_status)?.let {
                    tile.mqtt.lastReceive?.time.let { lr ->
                        val t = Date().time - (lr ?: 0)
                        if (lr != null) it.text = (t / 1000).let { s ->
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
            (activity as MainActivity).fm.replaceWith(TileNewFragment())
        }

        val onUiChange: () -> Unit = {
            theme.apply(b.root, requireContext(), false)
            adapter.notifyDataSetChanged()
        }

        toolBarController = ToolBarController(
            adapter,
            b.dBar,
            b.dToolbar,
            b.dToolbarIcon,
            b.dEdit,
            b.dSwap,
            b.dRemove,
            b.dAdd,
            addOnClick,
            onUiChange
        )

        b.dMore.setOnClickListener {
            propertiesOnClick()
        }

        b.dTag.setOnTouchListener { v, e ->
            showLog(v, e)
            return@setOnTouchListener true
        }

        b.dStatus.setOnTouchListener { v, e ->
            showLog(v, e)
            return@setOnTouchListener true
        }

        fun switchDashboard(slideRight: Boolean) {
            var index = dashboardIndex - if (slideRight) 1 else -1
            if (index < 0) index = dashboards.lastIndex
            if (index > dashboards.lastIndex) index = 0

            if (setCurrentDashboard(index)) (activity as MainActivity).fm.replaceWith(
                DashboardFragment(), false, true, slideRight
            )
        }

        b.dLeft.setOnClickListener {
            switchDashboard(true)
        }

        b.dRight.setOnClickListener {
            switchDashboard(false)
        }

        var flingTouchdownX = 0f
        var flingTouchdownY = 0f

        b.dRoot.onInterceptTouch = { e ->
            if (e != null) {
                when (e.action) {
                    ACTION_DOWN -> {
                        flingTouchdownX = e.x
                        flingTouchdownY = e.y
                    }
                    ACTION_UP -> {
                        val flingLen = (flingTouchdownX - e.x) / screenWidth

                        if (abs(flingTouchdownY - e.y) < 120 &&
                            abs(flingLen) > 0.2 &&
                            e.eventTime - e.downTime in 30..300 &&
                            adapter.editMode.isNone
                        ) {
                            switchDashboard(flingLen < 0)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = if (screenVertical) 2 else 4

        adapter = TilesAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
            b.dRemove.clearAnimation()
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.dRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.dRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            tile = item
            (activity as MainActivity).fm.replaceWith(TilePropertiesFragment())
        }

        adapter.onItemLongClick = { item ->
            if (item is SliderTile && !item.dragCon || item !is SliderTile) {
                tile = item
                (activity as MainActivity).fm.replaceWith(TilePropertiesFragment())
            }
        }

        adapter.submitList(dashboard.tiles)

        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
    }

    private fun setupLogRecyclerView() {
        val adapter = LogAdapter(requireContext())
        adapter.setHasStableIds(true)
        adapter.submitList(dashboard.log.list)

        val layoutManager = LinearLayoutManager(context)

        layoutManager.reverseLayout = true

        b.dLogRecyclerView.layoutManager = layoutManager
        b.dLogRecyclerView.adapter = adapter
    }

//----------------------------------------------------------------------------------------------

    private fun propertiesOnClick() {
        (activity as MainActivity).fm.replaceWith(DashboardPropertiesFragment())
    }

//----------------------------------------------------------------------------------------------

    private var showLogTouchdownY = 0f
    private fun showLog(v: View, e: MotionEvent) {
        v.performClick()

        when (e.action) {
            0 -> showLogTouchdownY = e.rawY
            2 -> {
                val lp = b.dLog.layoutParams
                val ldp = b.dLogBar.layoutParams

                lp.height = (e.rawY - showLogTouchdownY).toInt().let {
                    when {
                        it <= 0 -> 0
                        it <= (.9 * screenHeight) -> it
                        else -> {
                            dashboard.log.flush()
                            (.9 * screenHeight).toInt()
                        }
                    }
                }

                ldp.width = lp.height.let {
                    when {
                        it <= 0 -> 0
                        it <= (.9 * screenHeight) -> {
                            val max = screenHeight * .9
                            val per = it / max
                            (screenWidth * 0.8 - (screenWidth * 0.8 * per)).toInt()
                        }
                        else -> 0
                    }
                }

                b.dLog.layoutParams = lp
                b.dLogBar.layoutParams = ldp
            }
            else -> {
                val logAnimator = ValueAnimator.ofInt(b.dLog.measuredHeight, 0)
                logAnimator.duration = 500L

                logAnimator.addUpdateListener {
                    val animatedValue = logAnimator.animatedValue as Int
                    val layoutParams = b.dLog.layoutParams
                    layoutParams.height = animatedValue
                    b.dLog.layoutParams = layoutParams
                }
                logAnimator.start()

                val logBarAnimator = ValueAnimator.ofInt(
                    b.dLogBar.measuredWidth,
                    (.8 * screenWidth).toInt()
                )
                logBarAnimator.duration = 500L

                logBarAnimator.addUpdateListener {
                    val animatedValue = logBarAnimator.animatedValue as Int
                    val layoutParams = b.dLogBar.layoutParams
                    layoutParams.width = animatedValue
                    b.dLogBar.layoutParams = layoutParams
                }
                logBarAnimator.start()
            }
        }
    }
}