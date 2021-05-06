package com.netDashboard.dashboard_settings_activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.netDashboard.R
import com.netDashboard.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.DashboardSettingsActivityBinding
import com.netDashboard.main_activity.MainActivity
import com.netDashboard.margins
import com.netDashboard.tiles.Tiles
import com.netDashboard.toPx

class DashboardSettingsActivity : AppCompatActivity() {
    private lateinit var b: DashboardSettingsActivityBinding
    private lateinit var settings: DashboardSettings

    private lateinit var dashboardName: String
    private lateinit var dashboardFileName: String
    private lateinit var dashboardSettingsFileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = DashboardSettingsActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        dashboardName = intent.getStringExtra("dashboardName") ?: ""
        dashboardFileName = intent.getStringExtra("dashboardFileName") ?: ""
        dashboardSettingsFileName = intent.getStringExtra("dashboardSettingsFileName") ?: ""

        if (dashboardName.isEmpty() || dashboardFileName.isEmpty() || dashboardSettingsFileName.isEmpty()) {
            Intent(this, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }

        settings = DashboardSettings().getSettings(dashboardSettingsFileName)

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
                val list = Tiles().getList(dashboardFileName)

                for ((i, t) in list.withIndex()) {
                    if(t.width > b.span.value) {
                        list[i].width = b.span.value.toInt()
                    }
                }

                settings.spanCount = b.span.value.toInt()
                Tiles().saveList(list, dashboardFileName)

                b.warningSpan.visibility = View.GONE
                b.buttonApplySpan.visibility = View.GONE
            }

            val snackBarView = snackbar.view
            snackBarView.translationY = -20.toPx().toFloat()
            snackbar.show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        DashboardSettings().saveSettings(settings, dashboardSettingsFileName)

        Intent(this, DashboardActivity::class.java).also {
            it.putExtra("dashboardName", dashboardName)
            finish()
            startActivity(it)
        }
    }

    fun checkSpan(span: Int): Boolean {
        val list = Tiles().getList(dashboardFileName)

        for ((i, t) in list.withIndex()) {
            if (t.width > span) {
                return false
            }
        }

        return true
    }
}