package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.activities.fragments.dashboard.DashboardPropertiesFragment
import com.netDashboard.app_on.Activity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards.Companion.save
import com.netDashboard.databinding.FragmentDashboardNewBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.globals.G
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.globals.G.theme
import kotlin.random.Random

class DashboardNewFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentDashboardNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentDashboardNewBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(requireContext(), b.root)

        val name = kotlin.math.abs(Random.nextInt()).toString()
        val dashboard = Dashboard(name)
        G.dashboards.add(dashboard)
        G.dashboards.save()

        ForegroundService.service?.dgManager?.notifyDashboardAdded(dashboard)

        setCurrentDashboard(dashboard.id)
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.m_fragment, DashboardPropertiesFragment())
            addToBackStack(null)
            commit()
        }
    }
}