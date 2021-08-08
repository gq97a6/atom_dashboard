package com.netDashboard.activities.dashboard.properties

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.R
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityDashboardPropertiesBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import java.util.*
import kotlin.random.Random

class DashboardPropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardPropertiesBinding

    private lateinit var exitActivity: String
    private var dashboardId: Long = 0
    private lateinit var dashboard: Dashboard

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDashboardPropertiesBinding.inflate(layoutInflater)
        setContentView(b.root)

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                foregroundService = s
                onServiceReady()
            }
        })

        exitActivity = intent.getStringExtra("exitActivity") ?: ""
        dashboardId = intent.getLongExtra("dashboardId", 0)
        dashboard = Dashboards.get(dashboardId)

        b.dpName.setText(dashboard.name.lowercase(Locale.getDefault()))

        b.dpSpan.value = dashboard.spanCount.toFloat()
        b.dpSpan.callOnClick()
        b.dpSpanValue.text = dashboard.spanCount.toString()

        b.dpMqttSwitch.isChecked = dashboard.mqttEnabled
        mqttSwitchHandle(b.dpMqttSwitch.isChecked)

        b.dpMqttAddress.setText(dashboard.mqttAddress)
        dashboard.mqttPort.let {
            b.dpMqttPort.setText(if (it != -1) it.toString() else "")
        }

        dashboard.daemonGroup?.mqttd?.let {
            it.conHandler.isDone.observe(this) { isDone ->
                if (!dashboard.mqttEnabled) {
                    R.string.d_disconnected
                } else {
                    if (it.client.isConnected) {
                        b.dpMqttAddressStatus.background = b.dpMqttConnected.background
                        b.dpMqttPortStatus.background = b.dpMqttConnected.background

                        b.dpMqttAddressStatus.animate().alpha(1f)
                        b.dpMqttPortStatus.animate().alpha(1f)
                    } else {
                        if (isDone) {
                            b.dpMqttAddressStatus.background = b.dpMqttFailed.background
                            b.dpMqttPortStatus.background = b.dpMqttFailed.background

                            b.dpMqttAddressStatus.animate().alpha(1f)
                            b.dpMqttPortStatus.animate().alpha(1f)
                        } else {
                            b.dpMqttAddressStatus.background = b.dpMqttAttempting.background
                            b.dpMqttPortStatus.background = b.dpMqttAttempting.background

                            b.dpMqttAddressStatus.animate()
                                .alpha(1f)
                                .withEndAction {
                                    b.dpMqttAddressStatus.animate()
                                        .alpha(0f)
                                        ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                                        4000
                                }
                                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 200

                            b.dpMqttPortStatus.animate()
                                .alpha(1f)
                                .withEndAction {
                                    b.dpMqttPortStatus.animate()
                                        .alpha(0f)
                                        ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                                        4000
                                }
                                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 200
                        }
                    }
                }
            }
        }

        b.dpSpan.addOnChangeListener { _, value, _ ->
            b.dpSpanValue.text = value.toInt().toString()
            dashboard.spanCount = value.toInt()
        }

        b.dpMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchHandle(state)
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
    }

    override fun onPause() {
        super.onPause()
        Dashboards.save()
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
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
            it.putExtra("dashboardName", dashboard.name)
            startActivity(it)
            finish()
        }
    }

    private fun onServiceReady() {

    }

    fun checkSpan(span: Int): Boolean {
        val list = dashboard.tiles

        for (t in list) {
            if (t.width > span) {
                return false
            }
        }

        return true
    }

    private fun mqttSwitchHandle(state: Boolean) {
        b.dpMqtt.visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }

        dashboard.mqttEnabled = state
    }
}