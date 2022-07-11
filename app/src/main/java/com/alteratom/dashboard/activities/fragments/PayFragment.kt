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
import com.android.billingclient.api.BillingClient.BillingResponseCode.OK
import com.android.billingclient.api.BillingClient.BillingResponseCode.USER_CANCELED
import com.android.billingclient.api.BillingClient.ProductType.INAPP

class PayFragment : Fragment() {

    private lateinit var billingClient: BillingClient

    override fun onAttach(context: Context) {
        super.onAttach(context)

        billingClient = BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == OK && purchases != null) {
                    ConsumeParams
                        .newBuilder()
                        .setPurchaseToken(purchases[0].purchaseToken)
                        .build()
                        .let {
                            billingClient.consumeAsync(it) { billingResult, str ->
                                run {}
                            }
                        }
                } else if (billingResult.responseCode == USER_CANCELED) {
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

        billingClient.queryPurchasesAsync(QueryPurchasesParams()) { _, _ ->

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                var result by remember { mutableStateOf("NULL") }
                var isChecking by remember { mutableStateOf(false) }

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = colors.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                result,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )

                            BasicButton(onClick = {
                                lunchPurchaseFlow("atom_dashboard_pro")
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY", textAlign = TextAlign.Center, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun lunchPurchaseFlow(productId: String) {
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

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == OK) {
                    billingClient.queryProductDetailsAsync(queryDetails) { result, details ->
                        val result = billingClient.launchBillingFlow(
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

            override fun onBillingServiceDisconnected() {
                createToast(
                    requireContext(),
                    "Connection with Google Play failed"
                )
            }
        })
    }
}