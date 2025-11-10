package com.alteratom.dashboard.helper_objects

import com.alteratom.dashboard.app.AtomApp.Companion.aps
import java.io.File

object Pro {

    fun getLicenceStatus(): Boolean {
        return File("${aps.rootFolder}/license").exists()
    }

    fun createLocalLicence() {
        try {
            File("${aps.rootFolder}/license").writeText("")
            getLicenceStatus()
        } catch (_: Exception) {
        }
    }

    fun removeLocalLicence() {
        try {
            File("${aps.rootFolder}/license").delete()
            getLicenceStatus()
        } catch (_: Exception) {
        }
    }
}