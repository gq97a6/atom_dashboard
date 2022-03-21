package com.alteratom.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.alteratom.Activity
import com.alteratom.R
import com.alteratom.activities.fragments.DashboardFragment
import com.alteratom.activities.fragments.MainScreenFragment
import com.alteratom.databinding.ActivityMainBinding
import com.alteratom.G
import com.alteratom.G.setCurrentDashboard
import com.alteratom.G.settings

class MainActivity : AppCompatActivity() {
    lateinit var b: ActivityMainBinding
    val fm = FragmentManager()
    var onBackPressedBoolean: () -> Boolean = { false }

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
        if (!onBackPressedBoolean()) {
            if (!fm.popBackStack()) super.onBackPressed()
        }
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

            onBackPressedBoolean = { false }
        }

        fun popBackStack(stack: Boolean = false): Boolean {
            return if (backstack.isEmpty()) false
            else {
                replaceWith(backstack.removeLast(), stack)
                true
            }

            //onBackPressedBoolean = { false }
        }
    }
}
