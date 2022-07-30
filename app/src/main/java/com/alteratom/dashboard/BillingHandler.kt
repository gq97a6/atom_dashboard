package com.alteratom.dashboard

import android.app.Activity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.ConnectionState.*
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BillingHandler(private val activity: Activity) {

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

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>) {
        for (purchase in purchases) {
            onPurchased(purchase)
        }
    }

    private fun onPurchased(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        for (product in purchase.products) {
            when (product) {
                PRO -> {
                    createToast(activity, "Thanks for buying Pro!")
                    ProVersion.createLocalLicence()
                    purchase.acknowledge()
                }
                DON0, DON1, DON2 -> {
                    createToast(activity, "Thanks for the donation!")
                    purchase.consume()
                }
                else -> {}
            }
        }
    }

    suspend fun lunchPurchaseFlow(id: String) {
        if (!connectionHandler.awaitConnection()) {
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

    suspend fun getPurchases(callback: Boolean = true): MutableList<Purchase>? = coroutineScope {
        return@coroutineScope if (!connectionHandler.awaitConnection()) {
            createToast(activity, "Failed to connect")
            null
        } else withTimeoutOrNull(2000) {
            suspendCoroutine { continuation ->
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(INAPP)
                    .build()
                    .let {
                        client.queryPurchasesAsync(it) { result, history ->
                            if (callback) for (purchase in history) onPurchased(purchase)
                            continuation.resume(history)
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

    private fun createClient() {
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
            run {}
            when (client.connectionState) {
                CONNECTED -> client.endConnection()
                DISCONNECTED -> client.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {}
                    override fun onBillingServiceDisconnected() {}
                })
                CLOSED -> {
                    createClient()
                    handleDispatch()
                }
            }
        }


        suspend fun awaitConnection(timeout: Long = 5000): Boolean = withTimeoutOrNull(timeout) {
            while (!isDone()) delay(50)
            return@withTimeoutOrNull client.isReady
        } ?: false
    }
}