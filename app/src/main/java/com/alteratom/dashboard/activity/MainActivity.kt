package com.alteratom.dashboard.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.activity.fragment.DashboardFragment
import com.alteratom.dashboard.`object`.FragmentManager.fm
import com.alteratom.dashboard.`object`.G
import com.alteratom.dashboard.`object`.Setup
import com.alteratom.dashboard.`object`.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

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
                setCase()
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
