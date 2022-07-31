package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.FolderTree.rootFolder
import com.android.billingclient.api.Purchase
import kotlinx.coroutines.delay
import java.io.File
import kotlin.system.measureTimeMillis

object ProVersion {
    var status = false

    fun updateStatus() {
        status = File("$rootFolder/license").exists()
    }

    inline suspend fun checkPurchase(activity: Activity, onDone: () -> Unit) {
        var result: Purchase? = null
        val bh = BillingHandler(activity)
        measureTimeMillis {
            bh.enable()

            bh.getPurchases()?.find {
                it.products.contains(BillingHandler.PRO)
            }?.let {
                bh.onPurchased(it, false)
                result = it
            }

            bh.disable()
            bh.connectionHandler.awaitDone()
        }.let {
            delay(maxOf(10000 - it, 0))
            ///result?.let { for (product in it.products) bh.onPurchaseProcessed(product) }
            onDone()
        }
    }

    fun createLocalLicence() {
        try {
            File("$rootFolder/license").writeText("")
        } catch (e: Exception) {
        }
    }
}