package com.netDashboard.activities.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.DialogBuilder.dialogSetup
import com.netDashboard.R
import com.netDashboard.Transfer.showTransferPopup
import com.netDashboard.blink
import com.netDashboard.createToast
import com.netDashboard.databinding.DialogCopyBrokerBinding
import com.netDashboard.databinding.FragmentDashboardPropertiesBinding
import com.netDashboard.G.dashboard
import com.netDashboard.G.dashboards
import com.netDashboard.G.theme
import com.netDashboard.recycler_view.GenericAdapter
import com.netDashboard.recycler_view.GenericItem
import java.util.*
import kotlin.random.Random


class DashboardPropertiesFragment : Fragment(R.layout.fragment_dashboard_properties) {
    private lateinit var b: FragmentDashboardPropertiesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentDashboardPropertiesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        theme.apply(b.root, requireContext())
        viewConfig()

        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(viewLifecycleOwner) { isDone ->
                val v = b.dpMqttStatus
                v.text = if (dashboard.mqttEnabled) {
                    if (it.client.isConnected) {
                        v.clearAnimation()
                        "CONNECTED"
                    } else if (!isDone) {
                        if (v.animation == null) v.blink(-1, 400)
                        "ATTEMPTING"
                    } else {
                        v.clearAnimation()
                        "FAILED"
                    }
                } else {
                    v.clearAnimation()
                    "DISCONNECTED"
                }
            }
        }

        b.dpMqttSwitch.setOnCheckedChangeListener { _, state ->
            dashboard.mqttEnabled = state
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.dpName.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                dashboard.name =
                    it.ifBlank { kotlin.math.abs(Random.nextInt()).toString() }
            }
        }

        b.dpMqttAddress.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqttAddress != it) {
                    dashboard.mqttAddress = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPort.addTextChangedListener {
            val port = (it ?: "").toString().trim().toIntOrNull() ?: (-1)
            if (dashboard.mqttPort != port) {
                dashboard.mqttPort = port
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }
        }

        b.dpMqttCred.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttCredArrow.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttLogin.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqttUserName != it) {
                    dashboard.mqttUserName = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPass.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqttPass != it) {
                    dashboard.mqttPass = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttClientId.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                when {
                    it.isBlank() -> {
                        dashboard.mqttClientId = kotlin.math.abs(Random.nextInt()).toString()
                        b.dpMqttClientId.setText(dashboard.mqttClientId)
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                    dashboard.mqttClientId != it -> {
                        dashboard.mqttClientId = it
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        }

        b.dpMqttCopy.setOnClickListener {
            if (dashboards.size <= 1) {
                createToast(requireContext(), "No dashboards to copy from")
            } else {
                val dialog = Dialog(requireContext())
                val adapter = GenericAdapter(requireContext())

                val list = MutableList(dashboards.size) {
                    GenericItem(
                        R.layout.item_copy_broker
                    )
                }
                list.removeAt(dashboards.indexOf(dashboard))

                dialog.setContentView(R.layout.dialog_copy_broker)
                val binding = DialogCopyBrokerBinding.bind(dialog.findViewById(R.id.root))

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { _, holder, pos ->
                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0
                    val text = holder.itemView.findViewById<TextView>(R.id.icb_text)
                    text.text = dashboards[p].name.uppercase(Locale.getDefault())
                }

                adapter.onItemClick = {
                    val pos = adapter.list.indexOf(it)
                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0

                    dashboard.mqttAddress = dashboards[p].mqttAddress
                    dashboard.mqttPort = dashboards[p].mqttPort
                    dashboard.mqttUserName = dashboards[p].mqttUserName
                    dashboard.mqttPass = dashboards[p].mqttPass

                    viewConfig()
                    dialog.dismiss()
                }

                binding.dcbRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.dcbRecyclerView.adapter = adapter

                adapter.submitList(list)

                dialog.dialogSetup()
                theme.apply(binding.root)
                dialog.show()
            }
        }

        b.dpTransfer.setOnClickListener {
            if (dashboard.dg?.mqttd?.client?.isConnected == true) showTransferPopup(this)
            else createToast(requireContext(), "Connection required", 1000)
        }
    }

    private fun viewConfig() {
        b.dpName.setText(dashboard.name.lowercase(Locale.getDefault()))

        b.dpMqttSwitch.isChecked = dashboard.mqttEnabled

        b.dpMqttAddress.setText(dashboard.mqttAddress)
        dashboard.mqttPort.let {
            b.dpMqttPort.setText(if (it != -1) it.toString() else "")
        }

        b.dpMqttLogin.setText(dashboard.mqttUserName)
        b.dpMqttPass.setText(dashboard.mqttPass)

        b.dpMqttCredArrow.rotation = 180f
        b.dpMqttCred.visibility = View.GONE

        b.dpMqttClientId.setText(dashboard.mqttClientId)
    }

    private fun switchMqttCred() {
        b.dpMqttCred.let {
            it.visibility = if (it.isVisible) View.GONE else View.VISIBLE
            b.dpMqttPass.requestFocus()
            b.dpMqttPass.clearFocus()
            b.dpMqttCredArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
}