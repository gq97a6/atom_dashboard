package com.netDashboard.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.databinding.ActivitySplashScreenBinding
import com.netDashboard.folder_tree.FolderTree
import com.netDashboard.main_settings.MainSettings
import java.util.*
import kotlin.concurrent.schedule

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(b.root)

        //TMP
        val rootPath = "${filesDir.canonicalPath}/dashboard_data"
        FolderTree("$rootPath/test0").check()
        FolderTree("$rootPath/test1").check()
        FolderTree("$rootPath/test2").check()
        FolderTree("$rootPath/test3").check()
        FolderTree("$rootPath/test4").check()
        FolderTree("$rootPath/test5").check()
        //TMP

        val con = this
        Timer().schedule(500) {

            val settings = MainSettings(filesDir.canonicalPath).getSaved()

            if (settings.lastDashboardName != null) {

                Intent(con, DashboardActivity::class.java).also {
                    it.putExtra("dashboardName", settings.lastDashboardName)
                    overridePendingTransition(0, 0)
                    startActivity(it)
                    finish()
                }
            } else {
                Intent(con, MainActivity::class.java).also {
                    finish()
                    startActivity(it)
                }
            }
        }
    }
}
