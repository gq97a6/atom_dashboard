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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.lifecycleScope
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.compose.ComposeTheme
import com.android.billingclient.api.Purchase
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

            var priceTags by remember { mutableStateOf(mapOf<String, String>()) }

            remember {
                lifecycleScope.launch {
                    proCheckShow = true
                    billingHandler.getPriceTags(
                        listOf(
                            BillingHandler.PRO,
                            BillingHandler.DON0,
                            BillingHandler.DON1,
                            BillingHandler.DON2
                        )
                    ).let {
                        if (it != null) {
                            priceTags = it
                            scaleInitialValue = scale.value
                            scaleTargetValue = 0f
                            scaleDuration = 500
                            delay(500)
                            proCheckShow = false
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
                        Text(text = "Donate", fontSize = 45.sp, color = Theme.colors.color)
                        Text(
                            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium," +
                                    "totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. " +
                                    "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, " +
                                    "sed quia consequuntur magni dolores eos qui ratione voluptatem nesciunt.",
                            fontSize = 12.sp,
                            color = Theme.colors.a
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON0
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON0] ?: "",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON1
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON1] ?: "",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.DON2
                                    )
                                }
                            }, Modifier.weight(1f)) {
                                Text(
                                    priceTags[BillingHandler.DON2] ?: "",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }
                        }

                        Text(text = "Premium", fontSize = 45.sp, color = Theme.colors.color)
                        Text(
                            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium," +
                                    "totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. " +
                                    "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, " +
                                    "sed quia consequuntur magni dolores eos qui ratione voluptatem nesciunt.",
                            //modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = Theme.colors.a
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp)
                        ) {
                            BasicButton(onClick = {
                                lifecycleScope.launch {
                                    billingHandler.lunchPurchaseFlow(
                                        BillingHandler.PRO
                                    )
                                }
                            }, Modifier.fillMaxWidth(.3f)) {
                                Text(
                                    priceTags[BillingHandler.PRO] ?: "",
                                    fontSize = 10.sp,
                                    color = Theme.colors.a
                                )
                            }
                        }

                        Text(
                            "For delayed payment process use button below to process purchase after it succeeds.",
                            fontSize = 12.sp,
                            color = Theme.colors.a
                        )

                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BasicButton(
                                contentPadding = PaddingValues(13.dp),
                                border = BorderStroke(2.dp, Theme.colors.b),
                                modifier = Modifier
                                    .fillMaxWidth(.8f)
                                    .padding(top = 12.dp),
                                onClick = {
                                    if (!proCheckShow) {
                                        proCheckShow = true
                                        scaleInitialValue = 1f
                                        scaleTargetValue = .8f
                                        scaleDuration = 3000

                                        lifecycleScope.launch {
                                            billingHandler.checkPendingPurchases {
                                                if (it?.find { it.purchaseState != Purchase.PurchaseState.PURCHASED } == null)
                                                    createToast(
                                                        this@PayActivity,
                                                        "No pending purchase found"
                                                    )
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
                                Text("CHECK PENDING", fontSize = 10.sp, color = Theme.colors.a)
                            }
                        }

                        if (proCheckShow) {
                            Dialog(
                                { proCheckShow = true },
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
                                .height(40.dp)
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