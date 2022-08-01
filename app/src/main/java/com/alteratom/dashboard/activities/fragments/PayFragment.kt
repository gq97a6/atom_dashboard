package com.alteratom.dashboard.activities.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.BillingHandler.Companion.PRO
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme.Companion.colors
import com.alteratom.dashboard.compose.ComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PayFragment : Fragment() {

    lateinit var billingHandler: BillingHandler

    override fun onStart() {
        super.onStart()
        billingHandler = BillingHandler(requireActivity())
        billingHandler.enable()
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

                            var proCheckShow by remember { mutableStateOf(false) }
                            var scaleInitialValue by remember { mutableStateOf(1f) }
                            var scaleTargetValue by remember { mutableStateOf(.8f) }
                            var scaleDuration by remember { mutableStateOf(3000) }

                            val scale = rememberInfiniteTransition().animateFloat(
                                initialValue = scaleInitialValue,
                                targetValue = scaleTargetValue,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(scaleDuration),
                                    repeatMode = RepeatMode.Reverse,
                                )
                            )

                            val rotation = rememberInfiniteTransition().animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(3000),
                                    repeatMode = RepeatMode.Reverse,
                                )
                            )

                            Text(
                                "For delayed payment process use button below to process purchase after it succeeds",
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp,
                                color = colors.a
                            )

                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                border = BorderStroke(2.dp, colors.b),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                onClick = {
                                    if (!proCheckShow) {
                                        proCheckShow = true
                                        scaleInitialValue = 1f
                                        scaleTargetValue = .8f
                                        scaleDuration = 3000

                                        lifecycleScope.launch {
                                            ProVersion.checkPurchase(requireActivity()) {
                                                scaleInitialValue = scale.value
                                                scaleTargetValue = 0f
                                                scaleDuration = 1000
                                                delay(1000)
                                                proCheckShow = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("PRO CHECK", fontSize = 10.sp, color = colors.a)
                            }

                            if (proCheckShow) {
                                Dialog({ proCheckShow = true }) {
                                    Image(
                                        painterResource(
                                            if (Theme.isDark) R.drawable.ic_icon_light
                                            else R.drawable.ic_icon
                                        ), "",
                                        modifier = Modifier
                                            .padding(bottom = 100.dp)
                                            .scale(scale.value)
                                            .rotate(rotation.value)
                                            .size(300.dp),
                                        colorFilter = ColorFilter.tint(
                                            colors.color.copy(alpha = .4f),
                                            BlendMode.SrcAtop
                                        )
                                    )
                                }
                            }

                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                border = BorderStroke(2.dp, colors.b),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                onClick = {
                                    lifecycleScope.launch {
                                        BillingHandler(requireActivity()).apply {
                                            enable()

                                            getPurchases()?.find {
                                                it.products.contains(PRO)
                                            }?.let {
                                                it.consume()
                                            }

                                            disable()
                                            connectionHandler.awaitDone()
                                        }

                                        ProVersion.removeLocalLicence()
                                        createToast(requireContext(), "DONE")
                                    }
                                }
                            ) {
                                Text("PRO REMOVE", fontSize = 10.sp, color = colors.a)
                            }
                        }
                    }
                }
            }
        }
    }
}