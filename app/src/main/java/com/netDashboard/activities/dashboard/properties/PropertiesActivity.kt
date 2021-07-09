package com.netDashboard.activities.dashboard.properties

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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
        b.pMqttPort.setText(dashboard.mqttPort.toString())

        b.pSpan.addOnChangeListener { _, value, _ ->
            b.pSpanValue.text = value.toInt().toString()
            dashboard.spanCount = value.toInt()
        }

        b.pMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchHandle(state)
        }

        b.pMqttAddress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(cs: Editable) {}
            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                dashboard.mqttAddress = cs.toString()
            }
        })

        b.pMqttPort.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0) dashboard.mqttPort = cs.toString().toInt()
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
        if (state) {
            b.pMqttAddress.visibility = View.VISIBLE
            b.pMqttPort.visibility = View.VISIBLE
            b.pMqttAddressText.visibility = View.VISIBLE
            b.pMqttPortText.visibility = View.VISIBLE
        } else {
            b.pMqttAddress.visibility = View.GONE
            b.pMqttPort.visibility = View.GONE
            b.pMqttAddressText.visibility = View.GONE
            b.pMqttPortText.visibility = View.GONE
        }

        dashboard.mqttEnabled = state
    }
}