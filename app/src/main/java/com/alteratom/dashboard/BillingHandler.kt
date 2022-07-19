package com.alteratom.dashboard

import com.alteratom.dashboard.activities.MainActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED

class BillingHandler(val activity: MainActivity) {

    var client = BillingClient.newBuilder(activity)
        .enablePendingPurchases()
        .build()

    companion object {
        //Products ids
        var PRO = "atom_dashboard_pro"
    }

    fun initialize() {

    }

    fun lunchPurchaseFlow(id: String) {
        if (!client.isReady) return
        if (client.isFeatureSupported(PRODUCT_DETAILS).responseCode != OK) {
            createToast(activity, "Feature not supported")
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

    fun checkPurchaseStatus(id: String): Boolean {
        QueryPurchasesParams
            .newBuilder()
            .setProductType(INAPP)
            .build()
            .let {
                client.queryPurchasesAsync(it) { result, history ->
                    if (history.size == 0) return@queryPurchasesAsync
                    history.find { it.products.contains(id) && it.purchaseState == PURCHASED }
                    history.get(0).let { p ->
                        val p0 = p.developerPayload
                        val p1 = p.accountIdentifiers
                        val p2 = p.products
                        val p3 = p.purchaseState
                        val p4 = p.signature
                        val p5 = p.packageName
                        val p6 = p.isAcknowledged
                        val p7 = p.purchaseTime

                        run {}
                    }
                }
            }
        return false
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
}