package com.alteratom.dashboard.activities.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.hasShutdown
import com.alteratom.dashboard.objects.G.initializeGlobals
import com.alteratom.dashboard.objects.G.setCurrentDashboard
import com.alteratom.dashboard.objects.G.settings
import com.alteratom.dashboard.objects.G.theme
import com.alteratom.dashboard.restart
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SetupFragment : Fragment() {

    private var ready = MutableLiveData(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (hasShutdown) requireActivity().restart()
        (activity as MainActivity).doOverrideOnBackPress = { true }
    }

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

    override fun onStart() {
        super.onStart()

        CoroutineScope(Dispatchers.Default).launch {
            Log.i("ALTER_ATOM", "SETUP_FRAGMENT")

            //LEAVE IT THERE
            delay(50)

            (activity as? MainActivity)?.apply {
                service?.finishAndRemoveTask = { finishAndRemoveTask() }

                //Setup switchers
                TileSwitcher.activity = this
                FragmentSwitcher.activity = this

                //Setup on back press callback
                runOnUiThread {
                    onBackPressedDispatcher.addCallback(this) {
                        if (!doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
                    }
                }
            }

            //Disable foreground service if battery is optimized
            if (context?.isBatteryOptimized() != false) settings.foregroundService = false

            //Foreground service enabled by settings and battery usage is not optimised
            if (settings.foregroundService) foregroundServiceAllowed()
            else foregroundServiceDisallowed()
            //Foreground service disabled by settings or battery usage is optimised
        }
    }

    private fun foregroundServiceDisallowed() {
        Log.i("ALTER_ATOM", "SERVICE_DISALLOWED")

        //Disable foreground service as it should be
        if (service?.isStarted == true) ForegroundService.stop(activity as MainActivity)

        //Initialize globals with activity as context if not already
        if (!G.areInitialized) {
            (activity as MainActivity).apply { initializeGlobals(1) }

            if (settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
                fm.addBackstack(DashboardFragment())
            }
        }

        onSetupDone()
    }

    private suspend fun foregroundServiceAllowed() {
        Log.i("ALTER_ATOM", "SERVICE_ALLOWED")

        if (service?.isStarted == true) (activity as MainActivity).apply { //Service already launched
            Log.i("ALTER_ATOM", "SERVICE_ALREADY_RUNNING")

            if (G.areInitialized) onSetupDone()
            else { //Something went wrong | Globals should be initialized as service is

                Log.i("ALTER_ATOM", "NOT_INITIALIZED_ERROR")

                //Stop foreground service
                ForegroundService.stop(this@apply)

                //Restart in three seconds
                delay(1000)
                restart("STARTUP_ERROR_01")
            }
        } else (activity as MainActivity).apply { //Service not launched
            Log.i("ALTER_ATOM", "SERVICE_NOT_RUNNING")

            //Start foreground service
            ForegroundService.start(this@apply)

            //Wait for service
            if (ForegroundService.haltForService() == null) restart("STARTUP_ERROR_02") //restart if failed
            else {
                //Configure service
                service?.finishAndRemoveTask = { finishAndRemoveTask() }

                if (G.areInitialized && settings.startFromLast && setCurrentDashboard(settings.lastDashboardId)) {
                    fm.addBackstack(DashboardFragment())
                }

                //Initialize globals with service as context
                service?.initializeGlobals(1)

                onSetupDone()
            }
        }
    }

    private fun onSetupDone() {
        //Background billing check
        (activity as MainActivity).checkBilling()

        //Exit
        ready.postValue(true)
        fm.popBackstack(false, fadeLong)
    }
}