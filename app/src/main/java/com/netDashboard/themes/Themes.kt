package com.netDashboard.themes

import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.button.MaterialButton
import com.netDashboard.R
import com.netDashboard.getRandomColor


object Themes {
    fun set(context: Context) = context.setTheme(R.style.Theme_Dark)

    fun apply(b: ViewBinding) {
        fun loopViews(view: ViewGroup) {
            for (i in 0 until view.childCount) {
                val v: View = view.getChildAt(i)
                if (v is ViewGroup) {
                    loopViews(v)
                } else if (v is MaterialButton) {
                    v.backgroundTintList =
                        ColorStateList.valueOf(
                            getRandomColor()
                        )

                    Log.i("OUY", "CLASS: " + v.javaClass.toString())
                }
            }
        }

        loopViews(b.root as ViewGroup)
    }
}