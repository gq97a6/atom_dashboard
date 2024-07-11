package com.alteratom.dashboard.manager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.alteratom.R
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.activity.MainActivity.Companion.onGlobalTouch
import com.alteratom.dashboard.fragment.MainScreenFragment
import com.alteratom.dashboard.manager.FragmentManager.Animations.swap

class FragmentManager(val activity: MainActivity) {

    var backstack = mutableListOf<Fragment>(MainScreenFragment())
    private var currentFragment: Fragment = MainScreenFragment()
    var doOverrideOnBackPress: () -> Boolean = { false }

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
        if (activity.supportFragmentManager.isStateSaved) return

        //Reset callbacks
        onGlobalTouch = { false }
        doOverrideOnBackPress = { false }

        activity.supportFragmentManager.commit {
            animation?.invoke(this)
            replace(R.id.m_fragment, fragment)
            if (stack) backstack.add(currentFragment)
            currentFragment = fragment
        }
    }

    fun popBackstack(
        stack: Boolean = false,
        animation: ((FragmentTransaction) -> Unit?)? = swap
    ): Boolean {
        if (activity.supportFragmentManager.isStateSaved) return false
        if (backstack.isEmpty()) return false

        replaceWith(backstack.removeLast(), stack, animation)
        return true
    }
}