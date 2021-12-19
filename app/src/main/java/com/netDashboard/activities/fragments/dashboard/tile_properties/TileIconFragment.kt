package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.app_on.AppOn
import com.netDashboard.databinding.FragmentTileIconBinding

class TileIconFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentTileIconBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentTileIconBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }
}