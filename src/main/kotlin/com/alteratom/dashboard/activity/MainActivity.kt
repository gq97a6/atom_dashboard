package com.alteratom.dashboard.activity

import android.os.Bundle
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.fragment.DashboardFragment
import com.alteratom.dashboard.fragment.LoadingFragment
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.dashboard.manager.FragmentManager
import com.alteratom.dashboard.manager.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.observeUntil
import com.alteratom.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    companion object {
        lateinit var fm: FragmentManager
        var onGlobalTouch: (ev: MotionEvent?) -> Boolean = { false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //Apply padding to not cover status bar
        ViewCompat.setOnApplyWindowInsetsListener(b.mFragment) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }

        //Apply theme
        aps.theme.apply(b.root, this, false)

        //Intercept touch to handle gestures in whole app excluding PayActivity
        b.root.onInterceptTouch = { onGlobalTouch(it) }

        //Setup onBackPressCallback
        onBackPressedDispatcher.addCallback {
            if (!fm.doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
        }

        //Setup fragment manager
        fm = FragmentManager(this)

        //Launch loading fragment without stacking and no animation
        fm.replaceWith(LoadingFragment(), false, null)

        //Wait for aps to be initialized
        aps.isInitialized.observeUntil {
            if (it != true) return@observeUntil false

            ForegroundService.service?.finishAndRemoveTask = { finishAndRemoveTask() }

            if (aps.settings.startFromLast && aps.setCurrentDashboard(aps.settings.lastDashboardId)) {
                //Replace loading fragment with dashboard fragment
                fm.replaceWith(DashboardFragment(), false, fadeLong)
            } else {
                //Pop to main fragment
                fm.popBackstack(false, fadeLong)
            }

            //Remove observer
            return@observeUntil true
        }
    }

    override fun onPause() {
        aps.dashboards.saveToFile()
        aps.settings.saveToFile()
        aps.theme.saveToFile()

        super.onPause()
    }
}
