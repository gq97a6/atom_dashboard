package com.alteratom.dashboard.activities

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.Setup
import com.alteratom.dashboard.objects.Storage
import com.alteratom.dashboard.objects.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val a = this@MainActivity
        Setup.apply {
            paths(a)
            basicGlobals()

            //Apply theme
            G.theme.apply(b.root, a, false)

            CoroutineScope(Dispatchers.Default).launch {
                fragmentManager(a)
                showFragment()
                proStatus()
                billing(a)
                switchers(a)
                batteryCheck(a)
                service(a)
                globals()
                daemons(a)

                //Go straight to the dashboard
                if (G.settings.startFromLast && G.setCurrentDashboard(G.settings.lastDashboardId)) {
                    fm.addBackstack(DashboardFragment())
                }

                hideFragment()
            }
        }
    }

    override fun onStop() {
        G.dashboards.saveToFile()
        G.settings.saveToFile()
        G.theme.saveToFile()

        super.onStop()
    }
}
