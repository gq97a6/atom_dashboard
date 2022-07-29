package com.alteratom.dashboard.activities.fragments

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
import androidx.lifecycle.lifecycleScope
import com.alteratom.dashboard.BasicButton
import com.alteratom.dashboard.BillingHandler
import com.alteratom.dashboard.BillingHandler.Companion.PRO
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.createToast
import kotlinx.coroutines.launch

class PayFragment : Fragment() {

    lateinit var billingHandler: BillingHandler

    override fun onStart() {
        super.onStart()
        billingHandler = BillingHandler(requireActivity())
        billingHandler.enable()

        lifecycleScope.launch {
            val pro = billingHandler.getPurchases(false)?.find {
                it.products.contains(PRO)
            }

            createToast(requireContext(), if (pro != null) "PRO" else "NO PRO")
        }
    }

    override fun onStop() {
        super.onStop()
        billingHandler.disable()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theme.apply(context = requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                var text by remember { mutableStateOf("NULL") }
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
                                text,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )

                            BasicButton(onClick = {
                                lifecycleScope.launch { billingHandler.lunchPurchaseFlow("atom_dashboard_pro") }
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY PRO", textAlign = TextAlign.Center, color = Color.White)
                            }

                            BasicButton(onClick = {
                                lifecycleScope.launch { billingHandler.lunchPurchaseFlow("test_product_01") }
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY 01", textAlign = TextAlign.Center, color = Color.White)
                            }


                            BasicButton(onClick = {
                                lifecycleScope.launch { billingHandler.lunchPurchaseFlow("test_product_02") }
                            }, Modifier.padding(10.dp), enabled = !isChecking) {
                                Text("PAY 02", textAlign = TextAlign.Center, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}