package com.alteratom.dashboard.activity

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.fragment.DashboardFragment
import com.alteratom.dashboard.fragment.LoadingFragment
import com.alteratom.dashboard.fragment.MainScreenFragment
import com.alteratom.dashboard.helper_objects.FragmentManager
import com.alteratom.dashboard.helper_objects.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    companion object {
        var onGlobalTouch: (ev: MotionEvent?) -> Boolean = { false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Apply theme
        aps.theme.apply(b.root, this, false)

        //Intercept touch to handle gestures in whole app excluding PayActivity
        b.root.onInterceptTouch = { onGlobalTouch(it) }

        //Setup onBackPressCallback
        onBackPressedDispatcher.addCallback {
            if (!fm.doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
        }

        //Setup fragment manager
        fm.mainActivity = this
        if (aps.settings.startFromLast && aps.setCurrentDashboard(aps.settings.lastDashboardId)) {
            fm.addBackstack(DashboardFragment())
        }

        aps.isInitialized.observe(this) {
            if (it == true) {
                fm.popBackstack(false, fadeLong)
            }
        }

        fm.replaceWith(LoadingFragment(), animation = null)

        //if (!areNotificationsAllowed()) requestNotifications()
    }

    override fun onPause() {
        aps.dashboards.saveToFile()
        aps.settings.saveToFile()
        aps.theme.saveToFile()

        super.onPause()
    }
}
