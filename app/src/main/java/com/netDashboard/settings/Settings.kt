package com.netDashboard.settings

object Settings {
    var lastDashboardId: Long? = null
    var startFromLast: Boolean = false

    var colorPrimary = 0
    var colorSecondary = 0
    var colorBackground = 0

    init {
        getSaved()
    }

    fun save() {
    }

    private fun getSaved() {

    }
}