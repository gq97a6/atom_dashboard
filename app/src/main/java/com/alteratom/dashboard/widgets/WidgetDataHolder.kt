package com.alteratom.dashboard.widgets

import com.fasterxml.jackson.annotation.JsonTypeInfo

class WidgetDataHolder {
    val button = mutableMapOf<Int, ButtonWidgetProvider.Data>()
    val switch = mutableMapOf<Int, SwitchWidgetProvider.Data>()
    val short = mutableMapOf<Int, ShortWidgetProvider.Data>()

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
    )
    open class Data
}