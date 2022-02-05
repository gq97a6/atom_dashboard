package com.netDashboard.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.netDashboard.Activity
import com.netDashboard.R
import com.netDashboard.activities.fragments.DashboardFragment
import com.netDashboard.activities.fragments.MainScreenFragment
import com.netDashboard.databinding.ActivityMainBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.setCurrentDashboard
import com.netDashboard.globals.G.settings

class MainActivity : AppCompatActivity() {
    lateinit var b: ActivityMainBinding
    val fm = FragmentManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this)

        fm.replaceWith(MainScreenFragment(), false)
        if (settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
            fm.replaceWith(DashboardFragment())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Activity.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        Activity.onPause()
    }

    override fun onBackPressed() {
        if (!fm.popBackStack()) super.onBackPressed()
    }

    inner class FragmentManager {
        private var backstack = mutableListOf<Fragment>()
        private var currentFragment: Fragment = Fragment()

        fun replaceWith(fragment: Fragment, stack: Boolean = true) {
            supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.fragment_in,
                    R.anim.fragment_out,
                    R.anim.fragment_in,
                    R.anim.fragment_out
                )
                replace(R.id.m_fragment, fragment)
                if (stack) backstack.add(currentFragment)
                currentFragment = fragment
            }
        }

        fun popBackStack(stack: Boolean = false): Boolean {
            return if (backstack.isEmpty()) false
            else {
                replaceWith(backstack.removeLast(), stack)
                true
            }
        }
    }
}
