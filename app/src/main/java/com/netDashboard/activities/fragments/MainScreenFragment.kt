package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.fragments.dashboard.DashboardFragment
import com.netDashboard.activities.fragments.dashboard.DashboardPropertiesFragment
import com.netDashboard.app_on.Activity
import com.netDashboard.blink
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.FragmentMainScreenBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.globals.G
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.toolbarControl.ToolBarController

class MainScreenFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentMainScreenBinding

    private lateinit var adapter: DashboardAdapter
    private lateinit var toolBarController: ToolBarController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentMainScreenBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        G.theme.apply(requireContext(), b.root)

        val addOnClick: () -> Unit = {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, DashboardNewFragment())
                addToBackStack(null)
                commit()
            }
        }

        val onUiChange: (vg: ViewGroup) -> Unit = { vg ->
            G.theme.apply(requireContext(), vg)
        }

        toolBarController = ToolBarController(
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
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, SettingsFragment())
                addToBackStack(null)
                commit()
            }
        }
    }

    //override fun onBackPressed() {
    //    if (!adapter.editMode.isNone) {
    //        b.mLock.callOnClick()
    //    } else finishAffinity()
    //}

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(requireContext())
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.mPlaceholder.visibility = View.VISIBLE
            b.mRemove.clearAnimation()

            ForegroundService.service?.dgManager?.notifyDashboardRemoved(it)
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.mRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.mRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, DashboardPropertiesFragment())
                addToBackStack(null)
                commit()
            }
        }

        adapter.onItemClick = { item ->
            if (adapter.editMode.isNone) {
                setCurrentDashboard(item.id)
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.m_fragment, DashboardFragment())
                    addToBackStack(null)
                    commit()
                }
            }
        }

        adapter.onItemLongClick = { item ->
            setCurrentDashboard(item.id)
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, DashboardPropertiesFragment())
                addToBackStack(null)
                commit()
            }
        }

        adapter.submitList(G.dashboards)

        val layoutManager = LinearLayoutManager(requireContext())

        b.mRecyclerView.layoutManager = layoutManager
        b.mRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.mPlaceholder.visibility = View.VISIBLE
        }
    }
}