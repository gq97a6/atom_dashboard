package com.netDashboard.foreground_service.demons

abstract class Daemon {

    abstract val isWorking: Boolean
    abstract val isSentenced: Boolean
    abstract val isDead: Boolean

    abstract fun run()
    abstract fun kill()
    abstract fun sentence()
}