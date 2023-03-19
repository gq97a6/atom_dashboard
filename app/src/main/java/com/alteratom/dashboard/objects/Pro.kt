package com.alteratom.dashboard.objects

import com.alteratom.dashboard.objects.Storage.rootFolder
import java.io.File

object Pro {
    var status = false

    fun updateStatus() {
        status = File("$rootFolder/license").exists()
    }

    fun createLocalLicence() {
        try {
            File("$rootFolder/license").writeText("")
            updateStatus()
        } catch (_: Exception) {
        }
    }

    fun removeLocalLicence() {
        try {
            File("$rootFolder/license").delete()
            updateStatus()
        } catch (_: Exception) {
        }
    }
}