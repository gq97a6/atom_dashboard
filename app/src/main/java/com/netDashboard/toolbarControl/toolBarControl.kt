package com.netDashboard.toolbarControl

import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import com.netDashboard.R
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

class ToolBarController(
    private val adapter: BaseRecyclerViewAdapter<*>,
    private val bar: ConstraintLayout,
    private val lock: Button,
    private val edit: Button,
    private val swap: Button,
    private val remove: Button,
    add: Button,
    private val onAdd: () -> Unit,
    private val onUiChange: (vg: ViewGroup) -> Unit
) {
    private fun highlightOnly(button: Button) {
        remove.alpha = 0.4f
        swap.alpha = 0.4f
        edit.alpha = 0.4f
        button.alpha = 1f
    }

    fun toggleTools() {
        if (adapter.editMode.isNone) {
            adapter.editMode.setSwap()
            highlightOnly(swap)

            bar.animate()
                .translationY(-1.5f * bar.get(0).height.toFloat())
                .withEndAction { lock.setBackgroundResource(R.drawable.button_unlocked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        } else {
            adapter.editMode.setNone()

            remove.clearAnimation()
            bar.animate()
                .translationY(0f)
                .withEndAction { lock.setBackgroundResource(R.drawable.button_locked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        }

        onUiChange(lock.rootView as ViewGroup)
    }

    init {
        lock.setOnClickListener {
            toggleTools()
        }

        edit.setOnClickListener {
            highlightOnly(edit)
            adapter.editMode.setEdit()
        }

        swap.setOnClickListener {
            highlightOnly(swap)
            adapter.editMode.setSwap()
        }

        remove.setOnClickListener {
            if (!adapter.editMode.isRemove) {
                highlightOnly(remove)
                adapter.editMode.setRemove()
            } else {
                adapter.removeMarkedItems()
            }
        }

        add.setOnClickListener {
            onAdd()
        }
    }
}