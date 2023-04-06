package com.alteratom.dashboard.objects

import android.util.Log
import com.alteratom.dashboard.Logger
import com.alteratom.dashboard.objects.G.rootFolder
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
        } catch (e: Exception) {
             Logger.log(e.stackTraceToString())
        }
    }

    fun removeLocalLicence() {
        try {
            File("$rootFolder/license").delete()
            updateStatus()
        } catch (e: Exception) {
             Logger.log(e.stackTraceToString())
        }
    }
}