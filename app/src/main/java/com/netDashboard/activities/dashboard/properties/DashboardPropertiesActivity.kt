package com.netDashboard.activities.dashboard.properties

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.blink
import com.netDashboard.createToast
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityDashboardPropertiesBinding
import com.netDashboard.databinding.PopupCopyBrokerBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewItem
import java.util.*
import kotlin.random.Random

class DashboardPropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardPropertiesBinding

    private lateinit var exitActivity: String
    private var dashboardId: Long = 0
    private lateinit var dashboard: Dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppOn.create(this)

        exitActivity = intent.getStringExtra("exitActivity") ?: ""
        dashboardId = intent.getLongExtra("dashboardId", 0)
        dashboard = dashboards.byId(dashboardId)

        b = ActivityDashboardPropertiesBinding.inflate(layoutInflater)
        G.theme.apply(this, b.root)
        viewConfig()
        setContentView(b.root)

        dashboard.dg?.mqttd?.let {
            it.conHandler.isDone.observe(this) { isDone ->
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

        b.dpMqttCredBar.setOnClickListener {
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
                createToast(this, "No dashboards to copy from.")
            } else {
                val dialog = Dialog(this)
                val adapter = RecyclerViewAdapter(this)
                val theme = G.theme

                val list = MutableList(dashboards.size) {
                    RecyclerViewItem(
                        R.layout.item_copy_broker
                    )
                }
                list.removeAt(dashboards.indexOf(dashboard))

                dialog.setContentView(R.layout.popup_copy_broker)
                val binding = PopupCopyBrokerBinding.bind(dialog.findViewById(R.id.pcb_root))

                adapter.setHasStableIds(true)
                adapter.theme = theme
                adapter.onBindViewHolder = { _, holder, pos ->
                    val button = holder.itemView.findViewById<Button>(R.id.icb_button)

                    val p = pos + if (pos >= dashboards.indexOf(dashboard)) 1 else 0

                    button.setOnClickListener {
                        dashboard.mqttAddress = dashboards[p].mqttAddress
                        dashboard.mqttPort = dashboards[p].mqttPort
                        dashboard.mqttUserName = dashboards[p].mqttUserName
                        dashboard.mqttPass = dashboards[p].mqttPass

                        viewConfig()
                        dialog.hide()
                    }

                    button.text = dashboards[p].name.uppercase(Locale.getDefault())
                }

                binding.pcbRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.pcbRecyclerView.adapter = adapter

                adapter.submitList(list)
                dialog.show()
                theme.apply(this, binding.root)
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
        b.dpMqttCred.visibility = GONE

        b.dpMqttClientId.setText(dashboard.mqttClientId)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOn.destroy()
    }

    override fun onPause() {
        super.onPause()
        AppOn.pause()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(
            this,
            when (exitActivity) {
                "DashboardActivity" -> DashboardActivity::class.java
                "MainActivity" -> MainActivity::class.java
                else -> MainActivity::class.java
            }
        ).also {
            it.putExtra("dashboardId", dashboard.id)
            startActivity(it)
        }
    }

    private fun switchMqttCred() {
        b.dpMqttCred.let {
            it.visibility = if (it.isVisible) GONE else VISIBLE
            b.dpMqttPass.requestFocus()
            b.dpMqttPass.clearFocus()
            b.dpMqttCredArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
}