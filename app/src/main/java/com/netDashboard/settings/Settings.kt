package com.netDashboard.settings

class Settings {

    var lastDashboardId: Long? = null
    var startFromLast: Boolean = false

    companion object {
        fun getSaved(): Settings {
            return Settings()
        }
    }

    fun save() {
    }
}