package com.alteratom.dashboard.objects

import com.alteratom.dashboard.objects.G.rootFolder
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