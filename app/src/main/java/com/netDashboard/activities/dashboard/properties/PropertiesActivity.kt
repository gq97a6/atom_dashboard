package com.netDashboard.activities.dashboard.properties

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivityPropertiesBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler

class PropertiesActivity : AppCompatActivity() {
    private lateinit var b: ActivityPropertiesBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityPropertiesBinding.inflate(layoutInflater)
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

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboards.get(dashboardName)!!

        b.pSpan.value = dashboard.spanCount.toFloat()
        b.pSpan.callOnClick()
        b.pSpanValue.text = dashboard.spanCount.toString()

        b.pMqttSwitch.isChecked = dashboard.mqttEnabled
        mqttSwitchHandle(b.pMqttSwitch.isChecked)

        b.pMqttAddress.setText(dashboard.mqttAddress)
        dashboard.mqttPort.let {
            b.pMqttPort.setText(if (it != -1) it.toString() else "")
        }

        dashboard.daemonGroup?.mqttd?.let {
            it.conHandler.isDone.observe(this) { isDone ->
                if (!dashboard.mqttEnabled) {
                    R.string.d_disconnected
                } else {
                    if (it.client.isConnected) {
                        b.pMqttAddressStatus.background = b.pMqttConnected.background
                        b.pMqttPortStatus.background = b.pMqttConnected.background

                        b.pMqttAddressStatus.animate().alpha(1f)
                        b.pMqttPortStatus.animate().alpha(1f)
                    } else {
                        if (isDone) {
                            b.pMqttAddressStatus.background = b.pMqttFailed.background
                            b.pMqttPortStatus.background = b.pMqttFailed.background

                            b.pMqttAddressStatus.animate().alpha(1f)
                            b.pMqttPortStatus.animate().alpha(1f)
                        } else {
                            b.pMqttAddressStatus.background = b.pMqttAttempting.background
                            b.pMqttPortStatus.background = b.pMqttAttempting.background

                            b.pMqttAddressStatus.animate()
                                .alpha(1f)
                                .withEndAction {
                                    b.pMqttAddressStatus.animate()
                                        .alpha(0f)
                                        ?.setInterpolator(AccelerateDecelerateInterpolator())?.duration =
                                        4000
                                }
                                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 200

                            b.pMqttPortStatus.animate()
                                .alpha(1f)
                                .withEndAction {
                                    b.pMqttPortStatus.animate()
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

        b.pSpan.addOnChangeListener { _, value, _ ->
            b.pSpanValue.text = value.toInt().toString()
            dashboard.spanCount = value.toInt()
        }

        b.pMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchHandle(state)
            dashboard.daemonGroup?.mqttd?.reinit()
        }

        b.pMqttAddress.addTextChangedListener(object : TextWatcher {
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

        b.pMqttPort.addTextChangedListener(object : TextWatcher {
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
        Dashboards.save(dashboardName)
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            finish()
            startActivity(it)
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
        b.pMqtt.visibility = if (state) {
            View.VISIBLE
        } else {
            View.GONE
        }

        dashboard.mqttEnabled = state
    }
}