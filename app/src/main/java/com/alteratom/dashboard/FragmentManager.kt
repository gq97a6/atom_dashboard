package com.alteratom.dashboard

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.alteratom.R
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.fragments.MainScreenFragment


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