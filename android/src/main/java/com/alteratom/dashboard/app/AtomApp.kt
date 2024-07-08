package com.alteratom.dashboard.app

import android.app.Application
import com.alteratom.dashboard.helper_objects.Setup


class AtomApp : Application() {
    companion object {
        val aps = AtomAppState()
        lateinit var app: AtomApp
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        Setup.initialize()
    }
}