package com.alteratom.dashboard.activities.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.alteratom.dashboard.BasicButton
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.createToast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.FeatureType.PRODUCT_DETAILS
import com.android.billingclient.api.BillingClient.ProductType.INAPP
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PayFragment : Fragment() {

    private lateinit var billingClient: BillingClient

    suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                withContext(Dispatchers.IO) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams.build()) {

                    }
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == OK && purchases != null) {
                    val p = purchases[0]
                    val p0 = p.developerPayload
                    val p1 = p.accountIdentifiers
                    val p2 = p.products
                    val p3 = p.purchaseState
                    val p4 = p.signature
                    val p5 = p.packageName
                    val p6 = p.isAcknowledged
                    val p7 = p.purchaseTime
                } else if (billingResult.responseCode == ITEM_ALREADY_OWNED) {
                    run {}
                } else {
                    run {}
                }
            }
            .enablePendingPurchases()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theme.apply(context = requireContext())

        //billingClient.acknowledgePurchase() //accept or will be refunded
        //billingClient.queryPurchasesAsync()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == OK) {
                    createToast(
                        requireContext(),
                        "Connection with Google Play successful"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                createToast(
                    requireContext(),
                    "Connection with Google Play failed"
                )
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var text by remember { mutableStateOf("NULL") }
                var isChecking by remember { mutableStateOf(false) }

                fun acknowledge() {
                    QueryPurchaseHistoryParams
                        .newBuilder()
                        .setProductType(INAPP)
                        .build()
                        .let {
                            billingClient.queryPurchaseHistoryAsync(it) { result, history ->
                                if (history?.size == 0) return@queryPurchaseHistoryAsync
                                history?.get(0)?.let { p ->
                                    AcknowledgePurchaseParams
                                        .newBuilder()
                                        .setPurchaseToken(p.purchaseToken)
                                        .build()
                                        .let {
                                            billingClient.acknowledgePurchase(it) {
                                                text = result.debugMessage
                                            }
                                        }
                                }
                            }
                        }
                }

                fun consume() {
                    QueryPurchaseHistoryParams
                        .newBuilder()
                        .setProductType(INAPP)
                        .build()
                        .let {
                            billingClient.queryPurchaseHistoryAsync(it) { result, history ->
                                if (history?.size == 0) return@queryPurchaseHistoryAsync
                                history?.get(0)?.let { p ->
                                    ConsumeParams
                                        .newBuilder()
                                        .setPurchaseToken(p.purchaseToken)
                                        .build()
                                        .let {
                                            billingClient.consumeAsync(it) { billingResult, str ->
                                                text = result.debugMessage
                                            }
                                        }
                                }
                            }
                        }
                }

                fun getPurchasesOnline() {
                    QueryPurchaseHistoryParams
                        .newBuilder()
                        .setProductType(INAPP)
                        .build()
                        .let {
                            billingClient.queryPurchaseHistoryAsync(it) { result, history ->
                                QueryPurchasesParams
                                    .newBuilder()
                                    .setProductType(INAPP)
                                    .build()
                                    .let {
                                        billingClient.queryPurchasesAsync(it) { result, history ->
                                            if (history.size == 0) return@queryPurchasesAsync
                                            history?.get(0)?.let { p ->
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
                            }
                        }
                }

                fun getHistory() {
                    QueryPurchaseHistoryParams
                        .newBuilder()
                        .setProductType(INAPP)
                        .build()
                        .let {
                            billingClient.queryPurchaseHistoryAsync(it) { result, history ->
                                if (history?.size == 0) return@queryPurchaseHistoryAsync
                                history?.get(0)?.let { p ->
                                    val p0 = p.developerPayload
                                    val p1 = p.products
                                    val p2 = p.signature
                                    val p3 = p.purchaseTime
                                    val p4 = p.purchaseToken

                                    run {}
                                }
                            }
                        }
                }

                fun getPurchasesOffline() {
                    QueryPurchasesParams
                        .newBuilder()
                        .setProductType(INAPP)
                        .build()
                        .let {
                            billingClient.queryPurchasesAsync(it) { result, history ->
                                if (history.size == 0) return@queryPurchasesAsync
                                history?.get(0)?.let { p ->
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
                }

                fun lunchPurchaseFlow(productId: String) {
                    if (billingClient.isReady) {
                        if (billingClient.isFeatureSupported(PRODUCT_DETAILS).responseCode != OK) {
                            createToast(requireContext(), "Feature not supported")
                            return
                        }

                        val queryDetails = QueryProductDetailsParams
                            .newBuilder()
                            .setProductList(
                                listOf(
                                    QueryProductDetailsParams.Product.newBuilder()
                                        .setProductId(productId)
                                        .setProductType(INAPP)
                                        .build()
                                )
                            ).build()

                        billingClient.queryProductDetailsAsync(queryDetails) { result, details ->
                            if (details.size == 0) return@queryProductDetailsAsync
                            billingClient.launchBillingFlow(
                                requireActivity(),
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
                }

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )

                            BasicButton(onClick = {
                                lunchPurchaseFlow("atom_dashboard_pro")
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY PRO", textAlign = TextAlign.Center, color = Color.White)
                            }


                            BasicButton(onClick = {
                                lunchPurchaseFlow("test_product_01")
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY 01", textAlign = TextAlign.Center, color = Color.White)
                            }


                            BasicButton(onClick = {
                                lunchPurchaseFlow("test_product_02")
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY 02", textAlign = TextAlign.Center, color = Color.White)
                            }

                            BasicButton(onClick = {
                                getHistory()
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("HISTORY", textAlign = TextAlign.Center, color = Color.White)
                            }

                            BasicButton(onClick = {
                                getPurchasesOnline()
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text(
                                    "PURCHASES ONLINE",
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }

                            BasicButton(onClick = {
                                getPurchasesOffline()
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text(
                                    "PURCHASES OFFLINE",
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }

                            BasicButton(onClick = {
                                acknowledge()
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text(
                                    "ACKNOWLEDGE",
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }

                            BasicButton(onClick = {
                                consume()
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("CONSUME", textAlign = TextAlign.Center, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}