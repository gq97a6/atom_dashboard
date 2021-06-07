package com.netDashboard.activities.dashboard.settings

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.databinding.DashboardSettingsActivityBinding
import com.netDashboard.foreground_service.ForegroundServiceHandler

class DashboardSettingsActivity : AppCompatActivity() {
    private lateinit var b: DashboardSettingsActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardSettingsActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val abyssHandler = ForegroundServiceHandler(this)
        abyssHandler.start()
        abyssHandler.bind()

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        b.span.value = settings.spanCount.toFloat()
        b.span.callOnClick()
        b.spanValue.text = settings.spanCount.toString()

        b.mqttAddress.setText(settings.mqttAddress)
        b.mqttPort.setText(settings.mqttPort.toString())

        b.span.addOnChangeListener { _, value, _ ->
            b.spanValue.text = value.toInt().toString()
            settings.spanCount = value.toInt()
        }

        b.mqttAddress.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                if (count > 0) settings.mqttAddress = cs.toString()
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
}