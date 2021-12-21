package com.netDashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.netDashboard.R
import com.netDashboard.databinding.FragmentSettingsBinding
import com.netDashboard.globals.G

class SettingsFragment : Fragment(R.layout.fragment_tile_new) {
    private lateinit var b: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        b = FragmentSettingsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewConfig()
        G.theme.apply(requireContext(), b.root)

        b.sLast.setOnCheckedChangeListener { _, state ->
            G.settings.startFromLast = state
        }

        b.sThemeEdit.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.m_fragment, ThemeFragment())
                addToBackStack(null)
                commit()
            }
        }

        b.sThemeIsDark.setOnCheckedChangeListener { _, state ->
            G.theme.a.isDark = state
            G.theme.apply(requireContext(), b.root)
        }
    }

    private fun viewConfig() {
        b.sLast.isChecked = G.settings.startFromLast
        b.sThemeIsDark.isChecked = G.theme.a.isDark
    }
}