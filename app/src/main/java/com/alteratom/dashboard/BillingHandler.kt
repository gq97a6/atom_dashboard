package com.alteratom.dashboard

import android.app.Activity
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.Pro
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.ConnectionState.*
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureTimeMillis

class BillingHandler(val activity: Activity) {

    internal var isEnabled = false

    internal lateinit var client: BillingClient

    private val connectionHandler = BillingConnectionHandler()

    companion object {
        //Products ids
        var PRO = "atom_dashboard_pro"
        var DON1 = "atom_dashboard_don1"
        var DON5 = "atom_dashboard_don5"
        var DON25 = "atom_dashboard_don25"

        fun MainActivity.checkBilling() {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                BillingHandler(this@checkBilling).apply {
                    enable()
                    checkPurchases(
                        0,
                        {
                            !it.isAcknowledged || (!Pro.status && it.products.contains(
                                BillingHandler.PRO
                            ))
                        }
                    )
                    disable()
                }

                if (!Pro.status) {
                    for (e in G.dashboards.slice(2 until G.dashboards.size)) {
                        e.mqtt.isEnabled = false
                        e.daemon.notifyOptionsChanged()
                    }
                }

                cancel()
            }
        }
    }

    init {
        createClient()
    }

    internal fun createClient() {
        client = BillingClient.newBuilder(activity)
            .setListener { billingResult, purchases ->
                if (purchases != null &&
                    (billingResult.responseCode == OK ||
                            billingResult.responseCode == ITEM_ALREADY_OWNED)
                ) {
                    for (purchase in purchases) {
                        onPurchased(purchase)
                        onPurchaseProcessed(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()
    }

    fun enable() {
        isEnabled = true
        connectionHandler.dispatch("enable")
    }

    fun disable() {
        isEnabled = false
        connectionHandler.dispatch("disable")
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

    fun onPurchased(purchase: Purchase) {
        settings.pendingPurchase = false
        if (purchase.purchaseState != PURCHASED) {
            settings.pendingPurchase = true
            return
        }
        for (product in purchase.products) {
            when (product) {
                PRO -> {
                    Pro.createLocalLicence()
                    if (!purchase.isAcknowledged) purchase.acknowledge()
                }
                DON1, DON5, DON25 -> {
                    if (!purchase.isAcknowledged) purchase.consume()
                }
            }
        }
    }

    fun onPurchaseProcessed(purchase: Purchase) {
        if (purchase.purchaseState != PURCHASED) {
            createToast(activity, "Payment in process, please wait")
            return
        }
        for (product in purchase.products) {
            when (product) {
                PRO -> createToast(activity, "Thanks for buying Pro!")
                DON1, DON5, DON25 -> createToast(activity, "Thanks for the donation!")
            }
        }
    }

    private suspend fun getProductDetails(id: String): MutableList<ProductDetails>? =
        coroutineScope {
            return@coroutineScope if (!connectionHandler.awaitDone()) {
                //createToast(activity, "Failed to connect")
                null
            } else withTimeoutOrNull(2000) {
                suspendCoroutine { continuation ->
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

                    client.queryProductDetailsAsync(queryDetails) { _, details ->
                        continuation.resume(details)
                    }
                }
            }
        }

    suspend fun getPurchases(timeout: Long = 2000): MutableList<Purchase>? = coroutineScope {
        return@coroutineScope if (!connectionHandler.awaitDone()) {
            //createToast(activity, "Failed to connect")
            null
        } else withTimeoutOrNull(timeout) {
            suspendCoroutine { continuation ->
                QueryPurchasesParams
                    .newBuilder()
                    .setProductType(INAPP)
                    .build()
                    .let {
                        client.queryPurchasesAsync(it) { _, history ->
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

    suspend fun getPriceTags(ids: List<String>): Map<String, String>? = withTimeoutOrNull(2000) {
        List(ids.size) {
            getProductDetails(ids[it])?.first() ?: return@withTimeoutOrNull null
        }.let { it ->
            it.map {
                it.productId to
                        (it.oneTimePurchaseOfferDetails?.formattedPrice
                            ?: return@withTimeoutOrNull null)
            }.toMap()
        }
    }

    suspend fun lunchPurchaseFlow(id: String) {
        getProductDetails(id)?.let {
            if (it.isEmpty()) return
            client.launchBillingFlow(
                activity,
                BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(
                        listOf(
                            BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(it.first())
                                .build()
                        )
                    )
                    .build()
            )
        }
    }

    suspend inline fun checkPurchases(
        eta: Long = 10000,
        filter: (Purchase) -> Boolean = { !it.isAcknowledged },
        onDone: (List<Purchase>?) -> Unit = {}
    ) {
        var result: List<Purchase>? = null

        measureTimeMillis {
            getPurchases()?.filter(filter)?.let {
                result = it
                for (purchase in it) onPurchased(purchase)
            }
        }.let {
            delay(maxOf(eta - it, 0))
            if (result != null) for (purchase in result!!) onPurchaseProcessed(purchase)
            onDone(result)
        }
    }

    inner class BillingConnectionHandler : ConnectionHandler() {

        override fun isDoneCheck(): Boolean = when (client.connectionState) {
            CONNECTED -> isEnabled
            CONNECTING -> false
            DISCONNECTED, CLOSED -> !isEnabled
            else -> true
        }

        override fun handleDispatch() {
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

        //Wait for connectionHandler to settle down
        suspend fun awaitDone(timeout: Long = 5000): Boolean = withTimeoutOrNull(timeout) {
            while (!isDoneCheck()) delay(50)
            return@withTimeoutOrNull client.isReady
        } ?: false
    }
}