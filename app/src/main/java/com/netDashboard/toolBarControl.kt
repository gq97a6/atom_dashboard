package com.netDashboard

import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter

fun toolBarControl(
    adapter: BaseRecyclerViewAdapter<*>,
    bar: LinearLayout,
    lock: Button,
    edit: Button,
    swap: Button,
    remove: Button,
    add: Button,
    onAdd: () -> Unit
) {
    fun highlightOnly(button: Button) {
        remove.alpha = 0.4f
        swap.alpha = 0.4f
        edit.alpha = 0.4f
        button.alpha = 1f
    }

    lock.setOnClickListener {
        if (adapter.editMode.isNone) {
            adapter.editMode.setEdit()
            highlightOnly(edit)

            bar.animate()
                .translationY(0f)
                .withEndAction { lock.setBackgroundResource(R.drawable.button_unlocked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        } else {
            adapter.editMode.setNone()

            remove.clearAnimation()
            bar.animate()
                .translationY(bar.height.toFloat())
                .withEndAction { lock.setBackgroundResource(R.drawable.button_locked) }
                .setInterpolator(AccelerateDecelerateInterpolator())?.duration = 300
        }
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