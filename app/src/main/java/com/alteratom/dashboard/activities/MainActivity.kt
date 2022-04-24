package com.alteratom.dashboard.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.alteratom.R
import com.alteratom.dashboard.Activity
import com.alteratom.dashboard.DashboardSwitcher.FragmentSwitcher
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.setCurrentDashboard
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.TileSwitcher.TileSwitcher
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import com.alteratom.dashboard.activities.fragments.MainScreenFragment
import com.alteratom.dashboard.activities.fragments.SplashScreenFragment
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    lateinit var b: ActivityMainBinding

    val fm = FragmentManager()
    var onBackPressedBoolean: () -> Boolean = { false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Activity.onCreate(this)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this, false)

        TileSwitcher.activity = this
        FragmentSwitcher.activity = this

        fm.replaceWith(MainScreenFragment(), false)
        supportFragmentManager.commit {
            replace(R.id.m_fragment, SplashScreenFragment())
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

        fun replaceWith(
            fragment: Fragment,
            stack: Boolean = true,
            slide: Boolean = false,
            slideRight: Boolean = false
        ) {
            supportFragmentManager.commit {
                if (slide) {
                    if (slideRight) {
                        setCustomAnimations(
                            R.anim.fragment_in_slide_right,
                            R.anim.fragment_out_slide_right,
                            R.anim.fragment_in_slide_right,
                            R.anim.fragment_out_slide_right
                        )
                    } else {
                        setCustomAnimations(
                            R.anim.fragment_in_slide_left,
                            R.anim.fragment_out_slide_left,
                            R.anim.fragment_in_slide_left,
                            R.anim.fragment_out_slide_left
                        )
                    }
                } else {
                    setCustomAnimations(
                        R.anim.fragment_in,
                        R.anim.fragment_out,
                        R.anim.fragment_in,
                        R.anim.fragment_out
                    )
                }
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
