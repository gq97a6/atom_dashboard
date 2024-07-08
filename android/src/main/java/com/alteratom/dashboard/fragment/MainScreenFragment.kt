package com.alteratom.dashboard.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.R
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.activity.MainActivity.Companion.fm
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.dashboard.manager.ToolbarManager
import com.alteratom.dashboard.proAlert
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.databinding.FragmentMainScreenBinding
import kotlin.math.abs
import kotlin.random.Random

class MainScreenFragment : Fragment(R.layout.fragment_main_screen) {
    private lateinit var b: FragmentMainScreenBinding

    private lateinit var adapter: RecyclerViewAdapter<Dashboard>
    private lateinit var toolBarManager: ToolbarManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentMainScreenBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        aps.theme.apply(b.root, requireContext(), false)

        setupRecyclerView()

        fm.doOverrideOnBackPress = {
            if (!adapter.editMode.isNone) {
                toolBarManager.toggleTools()
                true
            } else false
        }

        val addOnClick: () -> Unit = {
            if (aps.isLicensed || aps.dashboards.size < 2) {
                //Temporarily skip dashboard type selection
                val name = abs(Random.nextInt()).toString()
                val dashboard = Dashboard(name, Daemon.Type.MQTTD)
                aps.dashboards.add(dashboard)
                aps.dashboards.saveToFile()
                DaemonsManager.notifyAssigned(dashboard, requireContext())
                if (aps.setCurrentDashboard(dashboard.id)) {
                    fm.replaceWith(DashboardPropertiesFragment())
                }
                //fm.replaceWith(DashboardNewFragment())
            } else {
                createToast(requireContext(), "Too many dashboards")
                requireContext().proAlert(requireActivity())
            }
        }

        val onUiChange: () -> Unit = {
            aps.theme.apply(b.msToolbar, requireContext(), false)
        }

        toolBarManager = ToolbarManager(
            adapter,
            b.msBar,
            b.msToolbar,
            b.msToolbarIcon,
            b.msEdit,
            b.msSwap,
            b.msRemove,
            b.msAdd,
            addOnClick,
            onUiChange
        )

        b.msMore.setOnClickListener {
            fm.replaceWith(SettingsFragment())
        }
    }

    //----------------------------------------------------------------------------------------------

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(requireContext())
        adapter.setHasStableIds(true)

        adapter.onItemRemoved = {
            if (adapter.itemCount == 0) b.msPlaceholder.visibility = VISIBLE
            b.msRemove.clearAnimation()
            DaemonsManager.notifyDischarged(it)
        }

        adapter.onItemMarkedRemove = { count, marked ->
            if (marked && count == 1) b.msRemove.blink(duration = 200, minAlpha = 0.2f)
            if (!marked && count == 0) b.msRemove.clearAnimation()
        }

        adapter.onItemEdit = { item ->
            if (aps.setCurrentDashboard(item.id)) fm.replaceWith(DashboardPropertiesFragment())
        }

        adapter.onItemClick = { item ->
            if (adapter.editMode.isNone && aps.setCurrentDashboard(item.id))
                fm.replaceWith(DashboardFragment())
        }

        adapter.onItemLongClick = { item ->
            if (aps.setCurrentDashboard(item.id)) fm.replaceWith(DashboardPropertiesFragment())
        }

        adapter.editMode.onSet = {
            if (!aps.isLicensed && aps.dashboards.size > 2 && adapter.editMode.isSwap) {
                b.msRemove.callOnClick()
                createToast(requireContext(), "Too many dashboards")
                requireContext().proAlert(requireActivity())
            }
        }

        adapter.submitList(aps.dashboards)

        val layoutManager = LinearLayoutManager(requireContext())

        b.msRecyclerView.layoutManager = layoutManager
        b.msRecyclerView.adapter = adapter

        if (adapter.itemCount == 0) b.msPlaceholder.visibility = VISIBLE
    }
}