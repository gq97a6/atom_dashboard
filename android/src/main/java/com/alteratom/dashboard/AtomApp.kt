package com.alteratom.dashboard

import android.app.Application
import com.alteratom.dashboard.helper_objects.Setup


class AtomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Setup.initialize(this)
    }
}