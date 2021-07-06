package com.netDashboard.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.app_on_destroy.AppOnDestroy
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.folder_tree.FolderTree.rootFolder
import com.netDashboard.foreground_service.ForegroundService
import com.netDashboard.foreground_service.ForegroundServiceHandler
import com.netDashboard.settings.Settings
import com.netDashboard.tile.types.button.ButtonTile
import com.netDashboard.tile.types.slider.SliderTile

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    private lateinit var service: ForegroundService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)

        rootFolder = filesDir.canonicalPath.toString()
        FolderTree.build()

        //TMP
        FolderTree.buildDashboard("example_dashboard")
        FolderTree.buildDashboard("test0")
        FolderTree.buildDashboard("test1")
        FolderTree.buildDashboard("test2")
        //TMP

        Dashboards.getSaved()

        //TMP
        if (Dashboards.get("example_dashboard")?.tiles?.isEmpty() == true) {


            val t0 = ButtonTile()
            t0.text = "temp > max"
            t0.color = Color.parseColor("#40E0D0")
            t0.width = 2
            val t1 = ButtonTile()
            t1.text = "ink low"
            t1.color = Color.parseColor("#8B008B")
            t1.width = 2
            val t2 = ButtonTile()
            t2.text = "alert"
            t2.color = Color.parseColor("#FFDAB9")
            t2.width = 2
            val t3 = ButtonTile()
            t3.text = "err3\nbreak num1\ntemp = 10C\nlong timer set"
            t3.color = Color.parseColor("#BDB76B")
            t3.height = 2
            val t4 = ButtonTile()
            t4.color = Color.parseColor("#EE82EE")
            val t5 = ButtonTile()
            t5.color = Color.parseColor("#8FBC8F")
            val t6 = ButtonTile()
            t6.color = Color.parseColor("#800000")
            val t7 = ButtonTile()
            t7.color = Color.parseColor("#a8ffff")
            val t8 = ButtonTile()
            t8.color = Color.parseColor("#9eaae5")
            val t9 = ButtonTile()
            t9.color = Color.parseColor("#d3ca1d")
            val t10 = ButtonTile()
            t10.color = Color.parseColor("#00FF00")
            val t11 = ButtonTile()
            t11.color = Color.parseColor("#EEE8AA")
            val t12 = SliderTile()
            t12.color = Color.parseColor("#00FF00")
            t12.step = 1f
            t12.width = 2
            val t13 = ButtonTile()
            t13.color = Color.parseColor("#232323")
            t13.width = 2
            val t14 = SliderTile()
            t14.color = Color.parseColor("#f918d8")
            t14.step = 20f
            t14.width = 2
            t14.value = 20f
            val t15 = SliderTile()
            t15.color = Color.parseColor("#c8c5f9")
            t15.step = 5f
            t15.value = 45f
            val t16 = SliderTile()
            t16.color = Color.parseColor("#76e09a")
            t16.value = 10f
            val t17 = SliderTile()
            t17.color = Color.parseColor("#f9f97f")
            t17.step = .1f
            t17.value = 33.5f
            val t18 = SliderTile()
            t18.color = Color.parseColor("#224d7f")
            t18.step = .01f
            t18.value = 49.39f
            val t19 = SliderTile()
            t19.color = Color.parseColor("#f9f100")
            t19.step = 1f
            t19.value = 15f

            val tiles = mutableListOf(
                t0,
                t5,
                t3,
                t1,
                t6,
                t18,
                t7,
                t8,
                t9,
                t17,
                t10,
                t12,
                t11,
                t13,
                t16,
                t14,
                t15
            )
            Dashboards.get("example_dashboard")?.tiles = tiles
        }
        //TMP

        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }

        val foregroundServiceHandler = ForegroundServiceHandler(this)
        foregroundServiceHandler.start()
        foregroundServiceHandler.bind()

        foregroundServiceHandler.service.observe(this, { s ->
            if (s != null) {
                service = s
                //onServiceReady()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        AppOnDestroy.call()
    }

    private fun onServiceReady() {
        if (Settings.lastDashboardName != null) {

            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", Settings.lastDashboardName)
                overridePendingTransition(0, 0)
                startActivity(it)
                finish()
            }
        } else {

            Intent(this, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}
