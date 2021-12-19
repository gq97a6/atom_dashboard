package com.netDashboard.activities.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.databinding.ActivityTilePropertiesBinding
import com.netDashboard.databinding.FragmentDashboardBinding
import com.netDashboard.databinding.FragmentTileNewBinding

class TileNewFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentTileNewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTileNewBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}