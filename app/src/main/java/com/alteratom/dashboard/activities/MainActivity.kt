package com.alteratom.dashboard.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.alteratom.R
import com.alteratom.dashboard.ActivityHandler
import com.alteratom.dashboard.G
import com.alteratom.dashboard.activities.fragments.MainScreenFragment
import com.alteratom.dashboard.activities.fragments.SplashScreenFragment
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    var onBackPressedBoolean: () -> Boolean = { false }

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHandler.onCreate(this)

        fm = FragmentManager(this)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this, false)

        TileSwitcher.activity = this
        FragmentSwitcher.activity = this

        fm.replaceWith(SplashScreenFragment(), false, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHandler.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        ActivityHandler.onPause()
    }

    override fun onBackPressed() {
        if (!onBackPressedBoolean()) {
            if (!fm.popBackStack()) super.onBackPressed()
        }
    }

    class FragmentManager(private val mainActivity: MainActivity) {
        private var backstack = mutableListOf<Fragment>(MainScreenFragment())
        private var currentFragment: Fragment = MainScreenFragment()

        companion object Animations {
            val swap: (FragmentTransaction) -> Unit = {
                it.apply {
                    setCustomAnimations(
                        R.anim.fragment_in_swap,
                        R.anim.fragment_out_swap,
                        R.anim.fragment_in_swap,
                        R.anim.fragment_out_swap
                    )
                }
            }

            val fade: (FragmentTransaction) -> Unit = {
                it.apply {
                    setCustomAnimations(
                        R.anim.fragment_in,
                        R.anim.fragment_out,
                        R.anim.fragment_in,
                        R.anim.fragment_out
                    )
                }
            }

            val slideLeft: (FragmentTransaction) -> Unit = {
                it.apply {
                    setCustomAnimations(
                        R.anim.fragment_in_slide_left,
                        R.anim.fragment_out_slide_left,
                        R.anim.fragment_in_slide_left,
                        R.anim.fragment_out_slide_left
                    )
                }
            }

            val slideRight: (FragmentTransaction) -> Unit = {
                it.apply {
                    setCustomAnimations(
                        R.anim.fragment_in_slide_right,
                        R.anim.fragment_out_slide_right,
                        R.anim.fragment_in_slide_right,
                        R.anim.fragment_out_slide_right
                    )
                }
            }

            val fadeLong: (FragmentTransaction) -> Unit = {
                it.apply {
                    setCustomAnimations(
                        R.anim.splashscreen_in,
                        R.anim.splashscreen_out,
                        R.anim.splashscreen_in,
                        R.anim.splashscreen_out
                    )
                }
            }
        }

        fun replaceWith(
            fragment: Fragment,
            stack: Boolean = true,
            animation: ((FragmentTransaction) -> Unit?)? = swap
        ) {
            mainActivity.apply {
                supportFragmentManager.commit {
                    animation?.invoke(this)
                    replace(R.id.m_fragment, fragment)
                    if (stack) backstack.add(currentFragment)
                    currentFragment = fragment
                }

                onBackPressedBoolean = { false }
            }
        }

        fun popBackStack(
            stack: Boolean = false,
            animation: ((FragmentTransaction) -> Unit?)? = swap
        ): Boolean {
            return if (backstack.isEmpty()) false
            else {
                replaceWith(backstack.removeLast(), stack, animation)
                true
            }

            //mainActivity.apply { onBackPressedBoolean = { false } }
        }
    }
}
