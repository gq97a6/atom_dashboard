package com.alteratom.dashboard.helper_objects

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.WindowManager
import com.alteratom.R
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.databinding.DialogConfirmBinding

object DialogBuilder {
    inline fun Context.buildChanges(
        message: String,
        label: String,
        crossinline onDeny: () -> Unit = {},
        crossinline onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_confirm)
        val binding = DialogConfirmBinding.bind(dialog.findViewById(R.id.root))

        binding.dcConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        binding.dcDeny.setOnClickListener {
            onDeny()
            dialog.dismiss()
        }

        binding.dcConfirm.text = label
        binding.dcText.text = message

        dialog.dialogSetup()
        aps.theme.apply(binding.root)
        dialog.show()
    }

    inline fun Context.buildConfirm(
        message: String,
        label: String,
        textSize: Float = 20f,
        textAlign: Int = TEXT_ALIGNMENT_CENTER,
        crossinline onDeny: () -> Unit = {},
        crossinline onConfirm: () -> Unit
    ) {
        val dialog = Dialog(this)

        dialog.setContentView(R.layout.dialog_confirm)
        val binding = DialogConfirmBinding.bind(dialog.findViewById(R.id.root))

        binding.dcConfirm.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        binding.dcDeny.setOnClickListener {
            onDeny()
            dialog.dismiss()
        }

        binding.dcConfirm.text = label

        binding.dcText.text = message
        binding.dcText.textSize = textSize
        binding.dcText.textAlignment = textAlign

        dialog.dialogSetup()
        aps.theme.apply(binding.root)
        dialog.show()
    }

    fun Dialog.dialogSetup() {
        val a = this.window?.attributes
        a?.dimAmount = 0.9f
        this.window?.attributes = a
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        this.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }
}