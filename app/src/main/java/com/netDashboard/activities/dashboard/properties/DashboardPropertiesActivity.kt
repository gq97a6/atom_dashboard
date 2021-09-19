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
import androidx.recyclerview.widget.GridLayoutManager
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.activities.theme.ThemeActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.blink
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityDashboardPropertiesBinding
import com.netDashboard.databinding.PopupCopyBrokerBinding
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
        dashboard.resultTheme.apply(this, b.root)
        viewConfig()
        setContentView(b.root)

        dashboard.daemonGroup?.mqttd?.let {
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

        b.dpSpan.addOnChangeListener { _, value, _ ->
            b.dpSpanValue.text = value.toInt().toString()
            dashboard.spanCount = value.toInt()
        }

        b.dpMqttSwitch.setOnCheckedChangeListener { _, state ->
            dashboard.mqttEnabled = state
            dashboard.daemonGroup?.mqttd?.reinit()
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
                        dashboard.daemonGroup?.mqttd?.reinit()
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
                    dashboard.daemonGroup?.mqttd?.reinit()
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
                        dashboard.daemonGroup?.mqttd?.reinit()
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
                        dashboard.daemonGroup?.mqttd?.reinit()
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
                            dashboard.daemonGroup?.mqttd?.reinit()
                        }
                        dashboard.mqttClientId != it -> {
                            dashboard.mqttClientId = it
                            dashboard.daemonGroup?.mqttd?.reinit()
                        }
                        else -> {
                            return
                        }
                    }
                }
            }
        })

        b.dpThemeEdit.setOnClickListener {
            Intent(this, ThemeActivity::class.java).also {
                it.putExtra("exitActivity", "DashboardPropertiesActivity")
                it.putExtra("dashboardId", dashboard.id)
                startActivity(it)
            }
        }

        b.dpThemeUseOver.setOnCheckedChangeListener { _, state ->
            dashboard.theme.useOver = state
            dashboard.resultTheme.apply(this, b.root)
        }

        b.dpMqttCopy.setOnClickListener {
            val dialog = Dialog(this)
            val adapter = RecyclerViewAdapter(this)
            val theme = dashboard.resultTheme
            val list = MutableList(dashboards.size) {
                RecyclerViewItem(
                    R.layout.item_copy_broker
                )
            }

            dialog.setContentView(R.layout.popup_copy_broker)
            val binding = PopupCopyBrokerBinding.bind(dialog.findViewById(R.id.cb_root))

            adapter.theme = theme
            adapter.onBindViewHolder = { _, holder, pos ->
                val button = holder.itemView.findViewById<Button>(R.id.cb_button)
                button.text = dashboards[pos].name
            }

            binding.cbRecyclerView.layoutManager = GridLayoutManager(this, 1)
            binding.cbRecyclerView.adapter = adapter

            adapter.submitList(list)
            theme.apply(this, binding.root)
            dialog.show()
        }
    }

    private fun viewConfig() {
        b.dpName.setText(dashboard.name.lowercase(Locale.getDefault()))

        b.dpSpan.value = dashboard.spanCount.toFloat()
        b.dpSpan.callOnClick()
        b.dpSpanValue.text = dashboard.spanCount.toString()

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

        b.dpThemeUseOver.isChecked = dashboard.theme.useOver
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
            b.dpMqttCredArrow.animate()
                .rotation(if (it.isVisible) 0f else 180f)
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 250
        }
    }
}