package com.alteratom.dashboard

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.activities.MainActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingHandler(val activity: MainActivity) {

    var client = BillingClient.newBuilder(activity)
        .setListener { billingResult, purchases ->
            if (purchases != null &&
                (billingResult.responseCode == OK ||
                        billingResult.responseCode == ITEM_ALREADY_OWNED)
            ) {
                onPurchasesUpdated(billingResult, purchases)
            }
        }
        .enablePendingPurchases()
        .build()

    companion object {
        //Products ids
        var PRO = "atom_dashboard_pro"
        var DON0 = "atom_dashboard_pro"
        var DON1 = "atom_dashboard_pro"
        var DON2 = "atom_dashboard_pro"

    }

    fun initialize() {

    }

    fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>) {
        for (purchase in purchases) {
            onPurchased(purchase)
        }
    }

    fun onPurchased(purchase: Purchase) {
        if (!purchase.isAcknowledged) return
        for (product in purchase.products) {
            when (product) {
                PRO -> {
                    createToast(activity, "Thanks for buying Pro!")
                    ProVersion.createLocalLicence()
                    purchase.acknowledge()
                }
                DON0, DON1, DON2 -> {
                    createToast(activity, "Thanks the donation!")
                    purchase.consume()
                }
                else -> {}
            }
        }
    }

    fun lunchPurchaseFlow(id: String) {
        if (!client.isReady) return
        if (client.isFeatureSupported(PRODUCT_DETAILS).responseCode != OK) {
            createToast(activity, "Please update Google Play Store")
            return
        }

        val queryDetails = QueryProductDetailsParams
            .newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(id)
                        .setProductType(INAPP)
                        .build()
                )
            ).build()

        client.queryProductDetailsAsync(queryDetails) { result, details ->
            if (details.size == 0) return@queryProductDetailsAsync
            client.launchBillingFlow(
                activity,
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(details.first())
                                .build()
                        )
                    )
                    .build()
            )
        }
    }

    suspend fun test() = coroutineScope {
        checkConnection()
        launch {
            delay(1000L)
            println("World!")
        }
        println("Hello")
    }

    suspend fun checkPurchasesStatus(id: String): Boolean = suspendCoroutine { continuation ->
        runBlocking { if (!checkConnection()) continuation.resume(false) }
        QueryPurchasesParams
            .newBuilder()
            .setProductType(INAPP)
            .build()
            .let {
                client.queryPurchasesAsync(it) { result, history ->
                    if (history.size == 0) continuation.resume(false)
                    for (purchase in history) onPurchased(purchase)
                    //developerPayload
                    //accountIdentifiers
                    //products
                    //purchaseState
                    //signature
                    //packageName
                    //isAcknowledged
                    //purchaseTime
                }
            }
    }

    private fun Purchase.acknowledge() {
        AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(this.purchaseToken)
            .build()
            .let { client.acknowledgePurchase(it) {} }
    }

    private fun Purchase.consume() {
        ConsumeParams
            .newBuilder()
            .setPurchaseToken(this.purchaseToken)
            .build()
            .let { client.consumeAsync(it) { _, _ -> } }
    }

    private suspend fun checkConnection(): Boolean = suspendCoroutine { continuation ->
        if (!client.isReady) {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == OK) continuation.resume(true)
                }

                override fun onBillingServiceDisconnected() {
                    continuation.resume(false)
                }
            })
        } else continuation.resume(true)
    }

    inner class BillingConnectionHandler : ConnectionHandler() {
        override fun isDone(): Boolean = client.isReady

        override fun handleDispatch() {
            client.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {}

                override fun onBillingServiceDisconnected() {}
            })
        }

        suspend fun check(): Boolean = suspendCoroutine { continuation ->
            if (!client.isReady) connect()
            else continuation.resume(true)
        }
    }
}