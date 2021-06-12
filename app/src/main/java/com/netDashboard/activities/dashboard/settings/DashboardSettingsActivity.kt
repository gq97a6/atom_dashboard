package com.netDashboard.activities.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.DashboardSettingsActivityBinding
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler

class DashboardSettingsActivity : AppCompatActivity() {
    private lateinit var b: DashboardSettingsActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    private lateinit var foregroundService: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardSettingsActivityBinding.inflate(layoutInflater)
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

        b.span.value = settings.spanCount.toFloat()
        b.span.callOnClick()
        b.spanValue.text = settings.spanCount.toString()

        b.mqttSwitch.isChecked = settings.mqttEnabled
        mqttSwitchHandle(b.mqttSwitch.isChecked)

        b.mqttAddress.setText(settings.mqttAddress)
        b.mqttPort.setText(settings.mqttPort.toString())

        b.span.addOnChangeListener { _, value, _ ->
            b.spanValue.text = value.toInt().toString()
            settings.spanCount = value.toInt()
        }

        b.mqttSwitch.setOnCheckedChangeListener { _, state ->
            mqttSwitchHandle(state)
        }

        b.mqttAddress.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(cs: Editable) {
            }

            override fun beforeTextChanged(cs: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                settings.mqttAddress = cs.toString()
            }
        })

        b.mqttPort.addTextChangedListener(object : TextWatcher {

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
            b.mqttAddress.visibility = View.VISIBLE
            b.mqttPort.visibility = View.VISIBLE
            b.mqttAddressText.visibility = View.VISIBLE
            b.mqttPortText.visibility = View.VISIBLE
        } else {
            b.mqttAddress.visibility = View.GONE
            b.mqttPort.visibility = View.GONE
            b.mqttAddressText.visibility = View.GONE
            b.mqttPortText.visibility = View.GONE
        }

        settings.mqttEnabled = state
    }

    private fun onServiceReady() {

    }
}