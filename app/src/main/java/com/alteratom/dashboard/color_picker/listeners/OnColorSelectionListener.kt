package com.alteratom.dashboard.color_picker.listeners

interface OnColorSelectionListener {
    fun onColorSelected(color: Int)
    fun onColorSelectionStart(color: Int)
    fun onColorSelectionEnd(color: Int)
}