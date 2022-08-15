package com.alteratom.dashboard

import com.alteratom.dashboard.Storage.rootFolder
import java.io.File

object Pro {
    var status = false

    fun updateStatus() {
        status = true//File("$rootFolder/license").exists()
    }

    fun createLocalLicence() {
        try {
            File("$rootFolder/license").writeText("")
            updateStatus()
        } catch (e: Exception) {
        }
    }

    fun removeLocalLicence() {
        try {
            File("$rootFolder/license").delete()
            updateStatus()
        } catch (e: Exception) {
        }
    }
}