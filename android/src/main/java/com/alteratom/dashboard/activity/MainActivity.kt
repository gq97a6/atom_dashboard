package com.alteratom.dashboard.activity

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.activity.fragment.LoadingFragment
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.G
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    companion object {
        var onGlobalTouch: (ev: MotionEvent?) -> Boolean = { false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Apply theme
        G.theme.apply(b.root, this, false)

        //Intercept touch to handle gestures in whole app excluding PayActivity
        b.root.onInterceptTouch = { onGlobalTouch(it) }

        //Setup fragment manager
        fm.mainActivity = this

        //Setup onBackPressCallback
        onBackPressedDispatcher.addCallback {
            if (!fm.doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
        }

        fm.replaceWith(LoadingFragment())

        //fm.replaceWith(SetupFragment(), animation = null)
        //if (!areNotificationsAllowed()) requestNotifications()
        //if (G.settings.startFromLast && G.setCurrentDashboard(G.settings.lastDashboardId)) {
        //    fm.addBackstack(DashboardFragment())
        //}
    }

    override fun onPause() {
        G.dashboards.saveToFile()
        G.settings.saveToFile()
        G.theme.saveToFile()

        super.onPause()
    }
}
