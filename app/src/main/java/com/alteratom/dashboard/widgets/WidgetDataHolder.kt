package com.alteratom.dashboard.widgets

class WidgetDataHolder {
    val button = mutableMapOf<Int, ButtonWidgetProvider.Data>()
    val switch = mutableMapOf<Int, SwitchWidgetProvider.Data>()
    val short = mutableMapOf<Int, ShortWidgetProvider.Data>()
}