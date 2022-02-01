package com.netDashboard.activities.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.R
import com.netDashboard.blink
import com.netDashboard.createToast
import com.netDashboard.databinding.FragmentDashboardPropertiesBinding
import com.netDashboard.databinding.PopupCopyBrokerBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboard
import com.netDashboard.globals.G.dashboards
import com.netDashboard.globals.G.theme
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import java.util.*
import kotlin.random.Random


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

        theme.apply(b.root, requireContext())
        viewConfig()

        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(viewLifecycleOwner) { isDone ->
                val v = b.dpMqttStatus
                v.text = if (dashboard.mqttEnabled) {
                    if (it.client.isConnected()) {
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

        b.dpName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                cs.toString().trim().let {
                    dashboard.name =
                        if (cs.isBlank()) kotlin.math.abs(Random.nextInt()).toString() else it
                }
            }
        })

        b.dpMqttAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                cs.toString().trim().let {
                    if (dashboard.mqttAddress != it) {
                        dashboard.mqttAddress = it
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        })

        b.dpMqttPort.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                val port = cs.toString().trim().toIntOrNull() ?: (-1)
                if (dashboard.mqttPort != port) {
                    dashboard.mqttPort = port
                    dashboard.dg?.mqttd?.notifyOptionsChanged()
                }
            }
        })

        b.dpMqttCred.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttCredArrow.setOnClickListener {
            switchMqttCred()
        }

        b.dpMqttLogin.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                cs.toString().trim().let {
                    if (dashboard.mqttUserName != it) {
                        dashboard.mqttUserName = it
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        })

        b.dpMqttPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                cs.toString().trim().let {
                    if (dashboard.mqttPass != it) {
                        dashboard.mqttPass = it
                        dashboard.dg?.mqttd?.notifyOptionsChanged()
                    }
                }
            }
        })

        b.dpMqttClientId.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                cs.toString().trim().let {
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
                        else -> {
                            return
                        }
                    }
                }
            }
        })

        b.dpMqttCopy.setOnClickListener {
            if (dashboards.size <= 1) {
                createToast(requireContext(), "No dashboards to copy from.")
            } else {
                val dialog = Dialog(requireContext())
                val adapter = RecyclerViewAdapter(requireContext())

                val list = MutableList(G.dashboards.size) {
                    RecyclerViewItem(
                        R.layout.item_copy_broker
                    )
                }
                list.removeAt(dashboards.indexOf(dashboard))

                dialog.setContentView(R.layout.popup_copy_broker)
                val binding = PopupCopyBrokerBinding.bind(dialog.findViewById(R.id.pcb_root))

                adapter.setHasStableIds(true)
                adapter.onBindViewHolder = { _, holder, pos ->
                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0

                    adapter.onItemClick = {
                        dashboard.mqttAddress = dashboards[p].mqttAddress
                        dashboard.mqttPort = dashboards[p].mqttPort
                        dashboard.mqttUserName = dashboards[p].mqttUserName
                        dashboard.mqttPass = dashboards[p].mqttPass

                        viewConfig()
                        dialog.hide()
                    }

                    val text = holder.itemView.findViewById<TextView>(R.id.icb_text)
                    text.text = dashboards[p].name.uppercase(Locale.getDefault())
                }



                binding.pcbRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                binding.pcbRecyclerView.adapter = adapter

                adapter.submitList(list)
                dialog.show()

                val a = dialog.window?.attributes

                a?.dimAmount = 0.9f
                dialog.window?.setAttributes(a)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                theme.apply(binding.root)
            }
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