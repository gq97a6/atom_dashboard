package com.alteratom.dashboard.widgets

import com.fasterxml.jackson.annotation.JsonTypeInfo

class WidgetDataHolder {
    val button = mutableListOf<ButtonWidgetProvider.Data>()
    val switch = mutableListOf<SwitchWidgetProvider.Data>()
    val short = mutableListOf<ShortWidgetProvider.Data>()

    @JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
    )
    open class Data
}