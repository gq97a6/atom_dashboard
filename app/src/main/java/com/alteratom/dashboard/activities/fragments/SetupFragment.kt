package com.alteratom.dashboard.activities.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.alteratom.R
import com.alteratom.dashboard.BillingHandler
import com.alteratom.dashboard.G
import com.alteratom.dashboard.G.setCurrentDashboard
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Pro
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.compose.ComposeTheme
import com.alteratom.dashboard.foreground_service.ForegroundService
import com.alteratom.dashboard.foreground_service.ForegroundServiceHandler
import com.alteratom.dashboard.foreground_service.demons.DaemonsManager
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {

    var ready = MutableLiveData(false)

    @SuppressLint("CoroutineCreationDuringComposition", "RememberReturnType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        theme.apply(context = requireContext())
        return ComposeView(requireContext()).apply {
            setContent {
                var scaleInitialValue by remember { mutableStateOf(1f) }
                var scaleTargetValue by remember { mutableStateOf(.8f) }
                var scaleDuration by remember { mutableStateOf(2500) }

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
                        animation = tween(2500),
                        repeatMode = RepeatMode.Reverse,
                    )
                )

                ComposeTheme(Theme.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column {
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
                }

                remember {
                    ready.observe(activity as MainActivity) {
                        if (it == true) {
                            scaleDuration = 1000
                            scaleInitialValue = scale.value
                            scaleTargetValue = 0f
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startService()
    }

    fun startService() = (activity as MainActivity).apply {
        if (ForegroundService.service?.isStarted == true) onServiceStarted()
        else {
            val handler = ForegroundServiceHandler(this@apply)
            handler.service.observe(this@apply) { service ->
                if (service != null) {
                    ForegroundService.service?.finishAffinity = { finishAffinity() }
                    DaemonsManager.initialize()
                    TileSwitcher.activity = this
                    FragmentSwitcher.activity = this

                    onServiceStarted()
                }
            }

            handler.start()
            handler.bind()
        }
    }

    fun onServiceStarted() {
        //Background billing check
        GlobalScope.launch {
            BillingHandler(activity as MainActivity).apply {
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
                    e.mqttData.isEnabled = false
                    e.daemon.notifyOptionsChanged()
                }
            }
        }

        //Exit
        ready.postValue(true)
        if (settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
            fm.replaceWith(DashboardFragment(), false, fadeLong)
        } else fm.popBackStack(false, fadeLong)
    }
}