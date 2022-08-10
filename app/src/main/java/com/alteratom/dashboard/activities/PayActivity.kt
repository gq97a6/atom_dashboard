package com.alteratom.dashboard.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.compose.BasicButton
import com.alteratom.dashboard.compose.ComposeTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PayActivity : AppCompatActivity() {

    lateinit var billingHandler: BillingHandler

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHandler.onCreate(this, false)

        G.theme.apply(context = this)

        billingHandler = BillingHandler(this)
        billingHandler.enable()

        setContent {
            var showLoading by remember { mutableStateOf(false) }
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

            var priceTags by remember { mutableStateOf(mapOf<String, String>()) }

            remember {
                lifecycleScope.launch {
                    showLoading = true
                    billingHandler.getPriceTags(
                        listOf(
                            BillingHandler.PRO,
                            BillingHandler.DON1,
                            BillingHandler.DON5,
                            BillingHandler.DON25
                        )
                    ).let {
                        if (it != null) {
                            priceTags = it
                            scaleInitialValue = scale.value
                            scaleTargetValue = 0f
                            scaleDuration = 500
                            delay(500)
                            showLoading = false
                        } else MainActivity.fm.popBackStack()
                    }
                }
            }

            ComposeTheme(Theme.isDark) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = Theme.colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Donate", fontSize = 40.sp, color = Theme.colors.color)
                        Text(
                            "Developing apps takes time and effort.\n\n" +
                                    "If you like the app and wish to support its further development, here you can make a donation.",
                            fontSize = 13.sp,
                            color = Theme.colors.a
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON1
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON1] ?: "99.99$",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON5
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON5] ?: "99.99$",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON25
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON25] ?: "99.99$",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }
                        }

                        Text(
                            text = "Pro upgrade",
                            modifier = Modifier.padding(top = 16.dp),
                            fontSize = 40.sp,
                            color = Theme.colors.color
                        )
                        Text(
                            "Support further development and upgrade to pro.\n\n" +
                                    "Get access to unlimited number of dashboards\n" +
                                    "and other pro features added in future updates.",
                            modifier = Modifier.padding(top = 3.dp),
                            fontSize = 13.sp,
                            color = Theme.colors.a
                        )
                        Text(
                            "\nNote: free pro upgrade for beta testers, includes final version of the app.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Theme.colors.color
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            BasicButton(
                                onClick = {
                                    lifecycleScope.launch {
                                        billingHandler.lunchPurchaseFlow(
                                            BillingHandler.PRO
                                        )
                                    }
                                },
                                //enabled = !ProVersion.status,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    priceTags[BillingHandler.PRO] ?: "99.99$",
                                    //if (ProVersion.status) "OWNED"
                                    //else priceTags[BillingHandler.PRO] ?: "99.99$",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }
                        }


                        Text(
                            "\nNote: free pro upgrade for beta testers, does NOT include final version of the app.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 10.dp),
                            color = Theme.colors.color
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                border = BorderStroke(2.dp, Theme.colors.b),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                onClick = {
                                    if (!showLoading) {
                                        showLoading = true
                                        scaleInitialValue = 1f
                                        scaleTargetValue = .8f
                                        scaleDuration = 3000

                                        Pro.createLocalLicence()

                                        lifecycleScope.launch {
                                            delay(8000)
                                            createToast(
                                                this@PayActivity,
                                                "Temporary license created"
                                            )
                                            scaleInitialValue = scale.value
                                            scaleTargetValue = 0f
                                            scaleDuration = 1000
                                            delay(1000)
                                            showLoading = false
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    "GET PRO TEMPORARILY",
                                    fontSize = 10.sp,
                                    color = Theme.colors.b
                                )
                            }
                        }

                        Text(
                            "For delayed payment process use button\nbelow to process purchase after it succeeds",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 30.dp),
                            color = Theme.colors.b
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                border = BorderStroke(2.dp, Theme.colors.b),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                onClick = {
                                    if (!showLoading) {
                                        showLoading = true
                                        scaleInitialValue = 1f
                                        scaleTargetValue = .8f
                                        scaleDuration = 3000

                                        lifecycleScope.launch {
                                            billingHandler.checkPendingPurchases {
                                                if (it?.isEmpty() != false == true)
                                                    createToast(
                                                        this@PayActivity,
                                                        "No pending purchase found"
                                                    )
                                                scaleInitialValue = scale.value
                                                scaleTargetValue = 0f
                                                scaleDuration = 1000
                                                delay(1000)
                                                showLoading = false
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text("CHECK PENDING", fontSize = 10.sp, color = Theme.colors.b)
                            }
                        }

                        if (showLoading) {
                            Dialog(
                                { showLoading = true },
                                DialogProperties(usePlatformDefaultWidth = false)
                            ) {
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
                                        Theme.colors.color.copy(alpha = .4f),
                                        BlendMode.SrcAtop
                                    )
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingHandler.disable()
    }
}