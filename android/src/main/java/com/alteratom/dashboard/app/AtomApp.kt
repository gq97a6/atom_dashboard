package com.alteratom.dashboard.app

import android.app.Application
import com.alteratom.dashboard.helper_objects.Setup


class AtomApp : Application() {
    companion object {
        val aps = AtomAppState()
    }

    override fun onCreate() {
        super.onCreate()
        Setup.initialize(this)
    }
}