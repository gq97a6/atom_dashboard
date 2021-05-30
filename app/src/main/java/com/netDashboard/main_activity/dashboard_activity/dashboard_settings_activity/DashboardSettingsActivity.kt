package com.netDashboard.main_activity.dashboard_activity.dashboard_settings_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.main_activity.dashboard_activity.Dashboard
import com.netDashboard.main_activity.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.DashboardSettingsActivityBinding
import com.netDashboard.margins
import com.netDashboard.toPx

class DashboardSettingsActivity : AppCompatActivity() {
    private lateinit var b: DashboardSettingsActivityBinding

    private lateinit var dashboardName: String
    private lateinit var dashboard: Dashboard
    private lateinit var settings: Dashboard.Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardSettingsActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboard = Dashboard(filesDir.canonicalPath, dashboardName)
        settings = dashboard.settings

        b.span.value = settings.spanCount.toFloat()
        b.span.callOnClick()

        b.span.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                if (checkSpan(slider.value.toInt())) {
                    settings.spanCount = slider.value.toInt()

                    b.warningSpan.visibility = View.GONE
                    b.buttonApplySpan.visibility = View.GONE
                } else {
                    b.warningSpan.visibility = View.VISIBLE
                    b.buttonApplySpan.visibility = View.VISIBLE
                }
            }
        })

        b.buttonApplySpan.setOnClickListener {

            @SuppressLint("ShowToast")
            val snackbar = Snackbar.make(
                b.root,
                getString(R.string.snackbar_confirmation),
                Snackbar.LENGTH_LONG
            ).margins().setAction("YES") {
                val list = dashboard.tiles

                for ((i, t) in list.withIndex()) {
                    if (t.width > b.span.value) {
                        list[i].width = b.span.value.toInt()
                    }
                }

                settings.spanCount = b.span.value.toInt()
                dashboard.tiles = list

                b.warningSpan.visibility = View.GONE
                b.buttonApplySpan.visibility = View.GONE
            }

            val snackBarView = snackbar.view
            snackBarView.translationY = -20.toPx().toFloat()
            snackbar.show()
        }

        b.udpPort.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(cs: CharSequence, start: Int, before: Int, count: Int) {
                //if (count > 0) settings.udpPort = Integer.parseInt(cs.toString())
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()

        dashboard.settings = settings

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            finish()
            startActivity(it)
        }
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