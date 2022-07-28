package com.alteratom.dashboard

import android.app.Activity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.ConnectionState.*
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingHandler(val activity: Activity) {

    private var isEnabled = false

    private lateinit var client: BillingClient

    val connectionHandler = BillingConnectionHandler()

    companion object {
        //Products ids
        var PRO = "atom_dashboard_pro"
        var DON0 = "atom_dashboard_pro"
        var DON1 = "atom_dashboard_pro"
        var DON2 = "atom_dashboard_pro"
    }

    init {
        createClient()
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
        if (!runBlocking { return@runBlocking connectionHandler.requestConnection() }) {
            createToast(activity, "Failed to connect")
            return
        }

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

    suspend fun checkPurchasesStatus(id: String): Boolean? = suspendCoroutine { continuation ->
        if (!runBlocking { return@runBlocking connectionHandler.requestConnection() }) {
            createToast(activity, "Failed to connect")
            continuation.resume(null)
            return@suspendCoroutine
        }

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

    fun enable() {
        isEnabled = true
        connectionHandler.dispatch("enable")
    }

    fun disable() {
        isEnabled = false
        connectionHandler.dispatch("disable")
    }

    fun createClient() {
        client = BillingClient.newBuilder(activity)
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
    }

    inner class BillingConnectionHandler : ConnectionHandler() {

        override fun isDone(): Boolean = when (client.connectionState) {
            CONNECTED -> isEnabled
            CONNECTING -> false
            DISCONNECTED, CLOSED -> !isEnabled
            else -> true
        }

        override fun handleDispatch() {
            when (client.connectionState) {
                DISCONNECTED -> if (isEnabled) {
                    client.startConnection(object : BillingClientStateListener {
                        override fun onBillingSetupFinished(billingResult: BillingResult) {
                            createToast(activity, "Am connected")
                        }

                        override fun onBillingServiceDisconnected() {
                            createToast(activity, "I have failed")
                        }
                    })
                }
                CONNECTED -> if (!isEnabled) client.endConnection()
                CLOSED -> {
                    createClient()
                    handleDispatch()
                }
            }
        }

        suspend fun requestConnection(timeout: Long = 5000): Boolean =
            suspendCoroutine { continuation ->
                if (client.isReady) {
                    createToast(activity, "Already connected")
                    continuation.resume(true)
                    return@suspendCoroutine
                } else createToast(activity, "I have to connect")

                isEnabled = true
                dispatch("req_con")

                runBlocking {
                    val j1 = launch {
                        delay(timeout)
                        createToast(activity, "Timeout")
                    }
                    val j2 = launch {
                        while (!client.isReady) delay(50)
                        createToast(activity, "Done!")
                    }

                    j1.invokeOnCompletion { j2.cancel() }
                    j2.invokeOnCompletion { j1.cancel() }
                }

                continuation.resume(client.isReady)
            }

        fun dropConnection() {
            isEnabled = false
            dispatch("drop_con")
        }
    }
}