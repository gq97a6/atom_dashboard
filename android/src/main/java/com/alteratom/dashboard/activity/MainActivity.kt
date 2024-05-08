package com.alteratom.dashboard.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.activity.fragment.DashboardFragment
import com.alteratom.dashboard.activity.fragment.MainScreenFragment
import com.alteratom.dashboard.objects.FragmentManager.fm
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.analytics
import com.alteratom.dashboard.objects.Setup
import com.alteratom.dashboard.objects.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val activity = this@MainActivity
        Setup.apply {

            paths(activity)
            basicGlobals()

            //Apply theme
            G.theme.apply(b.root, activity, false)

            CoroutineScope(Dispatchers.Default).launch {
                fragmentManager(activity)
                showFragment()
                proStatus()
                billing(activity)
                switchers(activity)
                batteryCheck(activity)
                setCase()
                service(activity)
                globals()
                daemons(activity)

                //Go straight to the dashboard
                if (G.settings.startFromLast && G.setCurrentDashboard(G.settings.lastDashboardId)) {
                    fm.addBackstack(DashboardFragment())

                    //Force fix of DashboardFragment stacking in backstack
                    fm.backstack = mutableListOf(MainScreenFragment(), DashboardFragment())
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
