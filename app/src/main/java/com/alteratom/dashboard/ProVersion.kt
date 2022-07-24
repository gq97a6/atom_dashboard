package com.alteratom.dashboard

import com.alteratom.dashboard.G.settings

object ProVersion {
    var status = false

    fun updateStatus() {
        status = settings.isPro
    }

    fun createLocalLicence() {
        settings.isPro = true
    }

}