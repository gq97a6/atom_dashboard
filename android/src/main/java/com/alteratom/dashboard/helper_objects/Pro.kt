package com.alteratom.dashboard.helper_objects

import com.alteratom.dashboard.helper_objects.G.rootFolder
import java.io.File

object Pro {

    fun getLicenceStatus(): Boolean {
        return File("$rootFolder/license").exists()
    }

    fun createLocalLicence() {
        try {
            File("$rootFolder/license").writeText("")
            getLicenceStatus()
        } catch (_: Exception) {
        }
    }

    fun removeLocalLicence() {
        try {
            File("$rootFolder/license").delete()
            getLicenceStatus()
        } catch (_: Exception) {
        }
    }
}