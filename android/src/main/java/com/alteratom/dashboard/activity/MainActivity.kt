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
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.fragment.DashboardFragment
import com.alteratom.dashboard.fragment.LoadingFragment
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.databinding.ActivityMainBinding


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
        fm.mainActivity = this
        if (aps.settings.startFromLast && aps.setCurrentDashboard(aps.settings.lastDashboardId)) {
            fm.backstack.add(DashboardFragment())
        }

        //Launch loading fragment
        fm.replaceWith(LoadingFragment(), animation = null)
    }

    override fun onPause() {
        aps.dashboards.saveToFile()
        aps.settings.saveToFile()
        aps.theme.saveToFile()

        super.onPause()
    }
}
