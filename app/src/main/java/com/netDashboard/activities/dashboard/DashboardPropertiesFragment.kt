package com.netDashboard.activities.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.FragmentDashboardBinding
import com.netDashboard.databinding.FragmentDashboardPropertiesBinding
import com.netDashboard.databinding.FragmentTileNewBinding
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toolbarControl.ToolBarController

class DashboardPropertiesFragment : Fragment(R.layout.fragment_dashboard_properties) {
    private lateinit var b: FragmentDashboardPropertiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentDashboardPropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}