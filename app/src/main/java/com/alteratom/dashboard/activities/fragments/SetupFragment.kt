package com.alteratom.dashboard.activities.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import com.alteratom.dashboard.BillingHandler.Companion.checkBilling
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.ForegroundService.Companion.service
import com.alteratom.dashboard.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.compose_global.ComposeTheme
import com.alteratom.dashboard.objects.G.setCurrentDashboard
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.G.theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SetupFragment : Fragment() {

    private var ready = MutableLiveData(false)

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

        //TODO: check if has to be cancelled
        CoroutineScope(Dispatchers.Default).launch {
            if (service?.isStarted == true) onServiceStarted()
            else (activity as MainActivity).apply {
                //Create a service
                Intent(this@apply, ForegroundService::class.java).also {
                    it.action = "DASH"
                    this@apply.startForegroundService(it)
                }

                //Wait for service
                while (service?.isStarted != true) service?.finishAffinity = { finishAffinity() }

                //Exit
                onServiceStarted()
            }
        }
    }

    private fun onServiceStarted() {
        //Background billing check
        (activity as MainActivity).checkBilling()

        //Exit
        ready.postValue(true)
        if (settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
            fm.replaceWith(DashboardFragment(), false, fadeLong)
        } else fm.popBackStack(false, fadeLong)
    }
}