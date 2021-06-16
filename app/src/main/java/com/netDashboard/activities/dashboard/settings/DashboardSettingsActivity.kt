package com.netDashboard.activities.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.ActivityDashboardSettingsBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler

class DashboardSettingsActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardSettingsBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityDashboardSettingsBinding.inflate(layoutInflater)
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
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        b.dsSpan.value = settings.spanCount.toFloat()
        b.dsSpan.callOnClick()
        b.dsSpanValue.text = settings.spanCount.toString()

        b.dsMqttSwitch.isChecked = settings.mqttEnabled
        mqttSwitchHandle(b.dsMqttSwitch.isChecked)

        b.dsMqttAddress.setText(settings.mqttAddress)
        b.dsMqttPort.setText(settings.mqttPort.toString())

        b.dsSpan.addOnChangeListener { _, value, _ ->
            b.dsSpanValue.text = value.toInt().toString()
            settings.spanCount = value.toInt()
        }

        b.dsMqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchHandle(state)
        }

        b.dsMqttAddress.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(cs: Editable) {
            }

            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                settings.mqttAddress = cs.toString()
            }
        })

        b.dsMqttPort.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0) settings.mqttPort = cs.toString().toInt()
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            finish()
            startActivity(it)
        }
    }

    override fun onPause() {
        dashboard.settings = settings

        foregroundService.rerun(dashboard.name)

        super.onPause()
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
            b.dsMqttAddress.visibility = View.VISIBLE
            b.dsMqttPort.visibility = View.VISIBLE
            b.dsMqttAddressText.visibility = View.VISIBLE
            b.dsMqttPortText.visibility = View.VISIBLE
        } else {
            b.dsMqttAddress.visibility = View.GONE
            b.dsMqttPort.visibility = View.GONE
            b.dsMqttAddressText.visibility = View.GONE
            b.dsMqttPortText.visibility = View.GONE
        }

        settings.mqttEnabled = state
    }

    private fun onServiceReady() {

    }
}