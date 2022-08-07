package com.alteratom.dashboard.activities.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.G.tile
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.dashboard_properties.DashboardPropertiesFragment
import com.alteratom.dashboard.activities.fragments.tile_properties.TilePropertiesFragment
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.log.LogEntry
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.FragmentDashboardBinding
import com.alteratom.tile.types.slider.SliderTile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var b: FragmentDashboardBinding

    private lateinit var adapter: RecyclerViewAdapter<Tile>
    private lateinit var toolBarHandler: ToolBarHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settings.lastDashboardId = dashboard.id

        b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupLogRecyclerView()
        theme.apply(b.root, requireContext(), false)

        (activity as MainActivity).onBackPressedBoolean = {
            if (!adapter.editMode.isNone) {
                toolBarHandler.toggleTools()
                true
            } else {
                false
            }
        }

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Set dashboard status
        dashboard.daemon.let {
            it.isDone.observe(viewLifecycleOwner) { isDone ->
                b.dSslStatus.visibility = GONE

                when (it) {
                    is Mqttd -> {
                        b.dStatus.text = when (it.status) {
                            Mqttd.Status.DISCONNECTED -> "DISCONNECTED"
                            Mqttd.Status.FAILED -> "FAILED TO CONNECT"
                            Mqttd.Status.ATTEMPTING -> "ATTEMPTING"
                            Mqttd.Status.CONNECTED -> "CONNECTED"
                            Mqttd.Status.CONNECTED_SSL -> {
                                b.dSslStatus.visibility = VISIBLE
                                "CONNECTED"
                            }
                        }
                    }
                }
            }
        }

        if (dashboards.size < 2 || settings.hideNav) {
            b.dLeft.visibility = GONE
            b.dRight.visibility = GONE
        }

        lifecycleScope.launch {
            delay(100)
            while (true) {
                for (tile in adapter.list) {
                    tile.holder?.itemView?.findViewById<TextView>(R.id.t_status)?.let {
                        tile.mqttData.lastReceive?.time.let { lr ->
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
                delay(1000)
            }
        }

        val addOnClick: () -> Unit = {
            fm.replaceWith(TileNewFragment())
        }

        val onUiChange: () -> Unit = {
            theme.apply(b.dToolbar, requireContext(), false)
        }

        toolBarHandler = ToolBarHandler(
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

        b.dLeft.setOnClickListener {
            FragmentSwitcher.switch(true)
        }

        b.dRight.setOnClickListener {
            FragmentSwitcher.switch(false)
        }

        b.dRoot.onInterceptTouch = { e ->
            if (adapter.editMode.isNone) FragmentSwitcher.handle(e)
            else false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()

        if (ProVersion.status && dashboard.securityLevel > 0) b.dRecyclerView.visibility = GONE
        this.dashboardAuthentication { b.dRecyclerView.visibility = VISIBLE }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = if (screenVertical) 2 else 4

        adapter = RecyclerViewAdapter(requireContext(), spanCount)
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.dPlaceholder.visibility = VISIBLE
            b.dRemove.clearAnimation()
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.dRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.dRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            tile = item
            fm.replaceWith(TilePropertiesFragment())
        }

        adapter.onItemLongClick = { item ->
            if (item is SliderTile && !item.dragCon || item !is SliderTile) {
                tile = item
                fm.replaceWith(TilePropertiesFragment())
            }
        }

        adapter.submitList(dashboard.tiles)

        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) b.dPlaceholder.visibility = VISIBLE
    }

    private fun setupLogRecyclerView() {
        val adapter = RecyclerViewAdapter<LogEntry>(requireContext())
        adapter.setHasStableIds(true)
        adapter.submitList(dashboard.log.list)

        val layoutManager = LinearLayoutManager(context)

        layoutManager.reverseLayout = true

        b.dLogRecyclerView.layoutManager = layoutManager
        b.dLogRecyclerView.adapter = adapter
    }

//----------------------------------------------------------------------------------------------

    private fun propertiesOnClick() {
        fm.replaceWith(DashboardPropertiesFragment())
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