package com.alteratom.dashboard.objects

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.alteratom.R
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.activity.MainActivity.Companion.onGlobalTouch
import com.alteratom.dashboard.activity.fragment.MainScreenFragment
import com.alteratom.dashboard.objects.FragmentManager.Animations.swap

object FragmentManager {

    var backstack = mutableListOf<Fragment>()
    private var currentFragment: Fragment = MainScreenFragment()
    var doOverrideOnBackPress: () -> Boolean = { false }
    var mainActivity: MainActivity? = null
    var fm = FragmentManager

    object Animations {
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
        mainActivity?.apply {
            //Reset onGlobalTouch action
            onGlobalTouch = { false }
            supportFragmentManager.commit {
                animation?.invoke(this)
                replace(R.id.m_fragment, fragment)
                if (stack) backstack.add(currentFragment)
                currentFragment = fragment
            }
        }

        doOverrideOnBackPress = { false }
    }

    fun popBackstack(
        stack: Boolean = false,
        animation: ((FragmentTransaction) -> Unit?)? = swap
    ): Boolean {
        return if (backstack.isEmpty()) false
        else {
            replaceWith(backstack.removeLast(), stack, animation)
            true
        }
    }

    fun addBackstack(fragment: Fragment) = backstack.add(fragment)
}