package com.netDashboard.activities.fragments

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.blink
import com.netDashboard.databinding.FragmentDashboardBinding
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.settings
import com.netDashboard.globals.G.theme
import com.netDashboard.log.LogAdapter
import com.netDashboard.screenHeight
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toolbarControl.ToolBarController
import java.util.*

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

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!adapter.editMode.isNone) toolBarController.toggleTools()
                    else {
                        isEnabled = false
                        activity?.onBackPressed()
                    }
                }
            })

        //Set dashboard name
        b.dTag.text = dashboard.name.uppercase(Locale.getDefault())

        //Set dashboard status
        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(viewLifecycleOwner) { isDone ->
                b.dStatus.text = getString(
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

        fun updateTilesStatus() {
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
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        val spanCount = 2

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
            val fragment = TilePropertiesFragment()
            fragment.apply {
                arguments = Bundle().apply {
                    putInt("index", dashboard.tiles.indexOf(item))
                }
            }
            (activity as MainActivity).fm.replaceWith(fragment)
        }

        adapter.onItemLongClick = { item ->
            val fragment = TilePropertiesFragment()
            fragment.apply {
                arguments = Bundle().apply {
                    putInt("index", dashboard.tiles.indexOf(item))
                }
            }
            (activity as MainActivity).fm.replaceWith(fragment)
        }

        adapter.submitList(dashboard.tiles)

        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)

        //layoutManager.spanSizeLookup =
        //    object : GridLayoutManager.SpanSizeLookup() {
        //        override fun tSpanSize(position: Int): Int {
        //            return adapter.list[position].width
        //        }
        //    }

        b.dRecyclerView.layoutManager = layoutManager
        b.dRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) b.dPlaceholder.visibility = View.VISIBLE
    }

    private fun setupLogRecyclerView() {
        val adapter = LogAdapter(requireContext())
        adapter.setHasStableIds(true)
        adapter.submitList(dashboard.log.list)

        val layoutManager = LinearLayoutManager(context)

        layoutManager.stackFromEnd = true
        b.dLogRecyclerView.layoutManager = layoutManager
        b.dLogRecyclerView.adapter = adapter
    }

//----------------------------------------------------------------------------------------------

    private fun propertiesOnClick() {
        (activity as MainActivity).fm.replaceWith(DashboardPropertiesFragment())
    }

//----------------------------------------------------------------------------------------------

    private var showLogStartY = 0f
    private fun showLog(v: View, e: MotionEvent) {
        v.performClick()

        if (e.action == KeyEvent.ACTION_DOWN) showLogStartY = e.rawY

        if (e.action == KeyEvent.ACTION_UP) {
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
}