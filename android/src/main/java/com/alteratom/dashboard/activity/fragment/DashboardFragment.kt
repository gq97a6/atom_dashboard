package com.alteratom.dashboard.activity.fragment

import SliderTile
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.alteratom.BuildConfig
import com.alteratom.R
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.daemon.daemons.mqttd.Mqttd
import com.alteratom.dashboard.log.LogEntry
import com.alteratom.dashboard.manager.ToolbarManager
import com.alteratom.dashboard.objects.DialogBuilder.buildConfirm
import com.alteratom.dashboard.objects.FragmentManager.fm
import com.alteratom.dashboard.objects.G.dashboard
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.objects.G.tile
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.screenHeight
import com.alteratom.dashboard.screenVertical
import com.alteratom.dashboard.screenWidth
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.tile.Tile
import com.alteratom.databinding.FragmentDashboardBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private lateinit var b: FragmentDashboardBinding

    private lateinit var adapter: RecyclerViewAdapter<Tile>
    private lateinit var toolBarManager: ToolbarManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentDashboardBinding.inflate(inflater, container, false)
        return b.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupLogRecyclerView()
        theme.apply(b.root, requireContext(), false)

        fm.doOverrideOnBackPress = {
            if (!adapter.editMode.isNone) {
                toolBarManager.toggleTools()
                true
            } else false
        }

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Update dashboard status on change
        dashboard.daemon?.statePing?.observe(viewLifecycleOwner) {
            dashboard.daemon?.let { updateStatus(it) }
        }

        //Hide navigation arrows if required
        if (dashboards.size < 2 || settings.hideNav) {
            b.dLeft.visibility = GONE
            b.dRight.visibility = GONE
        }

        //Update tile timers
        lifecycleScope.launch {
            while (true) {
                for (tile in adapter.list) tile.updateTimer()
                delay(1000)
            }
        }

        val addOnClick: () -> Unit = {
            fm.replaceWith(TileNewFragment())
        }

        val onUiChange: () -> Unit = {
            theme.apply(b.dToolbar, requireContext(), false)
        }

        toolBarManager = ToolbarManager(
            adapter,
            b.dBar,
            b.dToolbar,
            b.dToolbarIcon,
            b.dEdit,
            b.dSwap,
            b.dRemove,
            b.dAdd,
            addOnClick,
            onUiChange,
        ) { b.dSwap.callOnClick() }

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

        if (settings.version < BuildConfig.VERSION_CODE) {
            with(activity as Context) {
                buildConfirm(
                    "We've made changes to how the app works in the background. Please check your settings for more information.",
                    "SETTINGS",
                    {
                        settings.version = BuildConfig.VERSION_CODE
                    },
                    {
                        settings.version = BuildConfig.VERSION_CODE
                        fm.replaceWith(SettingsFragment())
                    })
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
        settings.lastDashboardId = dashboard.id
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

            1 -> {
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

    //----------------------------------------------------------------------------------------------

    private fun updateStatus(daemon: Daemon) {
        b.dSslStatus.visibility = GONE
        when (daemon) {
            is Mqttd -> daemon.updateStatus()
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun Mqttd.updateStatus() {
        b.dStatus.text = when (this.state) {
            Mqttd.State.DISCONNECTED -> "DISCONNECTED"
            Mqttd.State.FAILED -> "FAILED TO CONNECT"
            Mqttd.State.ATTEMPTING -> "ATTEMPTING"
            Mqttd.State.CONNECTED -> "CONNECTED"
            Mqttd.State.CONNECTED_SSL -> {
                b.dSslStatus.visibility = VISIBLE
                "CONNECTED"
            }
        }
    }

    //----------------------------------------------------------------------------------------------
}