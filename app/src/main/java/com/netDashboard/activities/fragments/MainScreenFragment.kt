package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.R
import com.netDashboard.blink
import com.netDashboard.dashboard.DashboardAdapter
import com.netDashboard.databinding.FragmentMainScreenBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.globals.G.theme
import com.netDashboard.toolbarControl.ToolBarController

class MainScreenFragment : Fragment(R.layout.fragment_main_screen) {
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
        theme.apply(requireContext(), b.root)

        val addOnClick: () -> Unit = {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, DashboardNewFragment())
                addToBackStack(null)
                commit()
            }
        }

        val onUiChange: (vg: ViewGroup) -> Unit = { vg ->
            theme.apply(requireContext(), vg)
        }

        toolBarController = ToolBarController(
            adapter,
            b.msBar,
            b.msLock,
            b.msEdit,
            b.msSwap,
            b.msRemove,
            b.msAdd,
            addOnClick,
            onUiChange
        )

        b.msSettings.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, SettingsFragment())
                addToBackStack(null)
                commit()
            }
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        adapter = DashboardAdapter(requireContext())
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.msPlaceholder.visibility = View.VISIBLE
            b.msRemove.clearAnimation()

            ForegroundService.service?.dgManager?.notifyDashboardRemoved(it)
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.msRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.msRemove.clearAnimation()
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

        adapter.submitList(dashboards)

        val layoutManager = LinearLayoutManager(requireContext())

        b.msRecyclerView.layoutManager = layoutManager
        b.msRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) {
            b.msPlaceholder.visibility = View.VISIBLE
        }
    }
}