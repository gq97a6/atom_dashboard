package com.netDashboard.picker.listeners

interface OnColorSelectionListener {
    fun onColorSelected(color: Int)
    fun onColorSelectionStart(color: Int)
    fun onColorSelectionEnd(color: Int)
}