package com.alteratom.dashboard.activities.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alteratom.dashboard.DialogBuilder.dialogSetup
import com.alteratom.R
import com.alteratom.dashboard.Transfer.showTransferPopup
import com.alteratom.dashboard.blink
import com.alteratom.dashboard.createToast
import com.alteratom.databinding.DialogCopyBrokerBinding
import com.alteratom.databinding.FragmentDashboardPropertiesBinding
import com.alteratom.dashboard.G.dashboard
import com.alteratom.dashboard.G.dashboards
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.recycler_view.GenericAdapter
import com.alteratom.dashboard.recycler_view.GenericItem
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
                v.text = if (dashboard.mqtt.isEnabled) {
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
            dashboard.mqtt.isEnabled = state
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
                if (dashboard.mqtt.address != it) {
                    dashboard.mqtt.address = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPort.addTextChangedListener {
            val port = (it ?: "").toString().trim().toIntOrNull() ?: (-1)
            if (dashboard.mqtt.port != port) {
                dashboard.mqtt.port = port
                dashboard.dg?.mqttd?.notifyOptionsChanged()
            }
        }

        b.dpMqttSsl.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.ssl = state
            b.dpMqttSslTrustAll.visibility = if(dashboard.mqtt.ssl) VISIBLE else GONE
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.dpMqttSslTrustAll.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.sslTrustAll = state
            dashboard.dg?.mqttd?.notifyOptionsChanged()
        }

        b.dpMqttCred.setOnCheckedChangeListener { _, state ->
            dashboard.mqtt.includeCred = state
            dashboard.dg?.mqttd?.notifyOptionsChanged()
            switchMqttCred(!state)
        }

        b.dpMqttCredArrow.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttLogin.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqtt.username != it) {
                    dashboard.mqtt.username = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttPass.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                if (dashboard.mqtt.pass != it) {
                    dashboard.mqtt.pass = it
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        }

        b.dpMqttClientId.addTextChangedListener { it ->
            (it ?: "").toString().trim().let {
                when {
                    it.isBlank() -> {
                        dashboard.mqtt.clientId = kotlin.math.abs(Random.nextInt()).toString()
                        b.dpMqttClientId.setText(dashboard.mqtt.clientId)
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                    dashboard.mqtt.clientId != it -> {
                        dashboard.mqtt.clientId = it
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

                    dashboard.mqtt.address = dashboards[p].mqtt.address
                    dashboard.mqtt.port = dashboards[p].mqtt.port
                    dashboard.mqtt.username = dashboards[p].mqtt.username
                    dashboard.mqtt.pass = dashboards[p].mqtt.pass

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

        b.dpMqttSwitch.isChecked = dashboard.mqtt.isEnabled

        b.dpMqttAddress.setText(dashboard.mqtt.address)
        dashboard.mqtt.port.let {
            b.dpMqttPort.setText(if (it != -1) it.toString() else "")
        }

        b.dpMqttCred.isChecked = dashboard.mqtt.includeCred
        b.dpMqttLogin.setText(dashboard.mqtt.username)
        b.dpMqttPass.setText(dashboard.mqtt.pass)

        b.dpMqttSsl.isChecked = dashboard.mqtt.ssl
        b.dpMqttSslTrustAll.isChecked = dashboard.mqtt.sslTrustAll
        b.dpMqttSslTrustAll.visibility = if(dashboard.mqtt.ssl) VISIBLE else GONE

        b.dpMqttCredArrow.rotation = 180f
        b.dpMqttCredBox.visibility = View.GONE

        b.dpMqttClientId.setText(dashboard.mqtt.clientId)
    }

    private fun switchMqttCred(state: Boolean? = null) {
        b.dpMqttCredBox.let {
            b.dpMqttCredArrow.animate()
                .rotation(if (state ?: it.isVisible) 180f else 0f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250

            it.visibility = if (state ?: it.isVisible) View.GONE else View.VISIBLE
            b.dpMqttPass.requestFocus()
            b.dpMqttPass.clearFocus()
        }
    }
}