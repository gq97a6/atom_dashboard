package com.alteratom.dashboard.objects

import androidx.activity.addCallback
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.MainActivity
import com.alteratom.dashboard.activity.fragment.SetupFragment
import com.alteratom.dashboard.activity.fragment.SetupFragment.Companion.ready
import com.alteratom.dashboard.areNotificationsAllowed
import com.alteratom.dashboard.checkBilling
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.objects.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.objects.FragmentManager.fm
import com.alteratom.dashboard.objects.G.dashboards
import com.alteratom.dashboard.objects.Setup.SetupCase.ACTIVITY
import com.alteratom.dashboard.objects.Setup.SetupCase.ACTIVITY_COLD
import com.alteratom.dashboard.objects.Setup.SetupCase.ACTIVITY_TO_SERVICE
import com.alteratom.dashboard.objects.Setup.SetupCase.SERVICE
import com.alteratom.dashboard.objects.Setup.SetupCase.SERVICE_COLD
import com.alteratom.dashboard.objects.Setup.SetupCase.SERVICE_TO_ACTIVITY
import com.alteratom.dashboard.requestNotifications
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import kotlinx.coroutines.delay

//Sorted by order of execution setup sequence
object Setup {

    private var case = ACTIVITY

    enum class SetupCase { SERVICE, SERVICE_COLD, SERVICE_TO_ACTIVITY, ACTIVITY, ACTIVITY_COLD, ACTIVITY_TO_SERVICE }

    fun paths(activity: MainActivity) {
        G.rootFolder = activity.filesDir.canonicalPath.toString()
        G.path = mapOf(
            Theme::class to "${G.rootFolder}/theme",
            Settings::class to "${G.rootFolder}/settings",
            Dashboard::class to "${G.rootFolder}/dashboards"
        )
    }

    fun basicGlobals() {
        if (!G.areInitialized) {
            G.theme = Storage.parseSave() ?: Theme()
            G.settings = Storage.parseSave() ?: Settings()
        }
    }

    fun fragmentManager(activity: MainActivity) {
        activity.apply {
            fm.mainActivity = this
            onBackPressedDispatcher.addCallback {
                if (!fm.doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
            }
        }
    }

    fun showFragment() = fm.replaceWith(SetupFragment(), animation = null)

    fun proStatus() = Pro.updateStatus()

    fun billing(activity: MainActivity) = activity.checkBilling()

    fun switchers(activity: MainActivity) {
        TileSwitcher.activity = activity
        FragmentSwitcher.activity = activity
    }

    fun batteryCheck(activity: MainActivity) {
        //Disable foreground service if battery is optimized
        if (activity.isBatteryOptimized()) G.settings.fgEnabled = false
    }

    fun setCase() {
        case = if (ForegroundService.service?.isStarted == true) {
            if (G.settings.fgEnabled) SERVICE
            else SERVICE_TO_ACTIVITY
        } else {
            if (G.settings.fgEnabled) {
                if (G.areInitialized) ACTIVITY_TO_SERVICE
                else SERVICE_COLD
            } else {
                if (G.areInitialized) ACTIVITY
                else ACTIVITY_COLD
            }
        }
    }

    suspend fun service(activity: MainActivity) {
        when (case) {
            SERVICE_TO_ACTIVITY -> {
                //Discharge all daemons
                DaemonsManager.notifyAllDischarged()

                //Foreground service enabled by settings and battery usage is not optimised
                ForegroundService.stop(activity)
            }

            ACTIVITY_TO_SERVICE, SERVICE_COLD -> {
                //Discharge all daemons
                DaemonsManager.notifyAllDischarged()

                //Foreground service disabled by settings or battery usage is optimised
                ForegroundService.start(activity)
                ForegroundService.haltForService()

                //Configure service
                ForegroundService.service?.finishAndRemoveTask = { activity.finishAndRemoveTask() }
            }

            else -> {}
        }
    }

    fun globals() {
        if (!G.areInitialized) {
            dashboards = Storage.parseListSave()
            G.areInitialized = true
        }
    }

    fun permissions(activity: MainActivity) {
        activity.apply { if (!areNotificationsAllowed()) requestNotifications() }
    }

    fun daemons(activity: MainActivity) {
        when (case) {
            SERVICE_COLD, ACTIVITY_TO_SERVICE -> ForegroundService.service?.let {
                DaemonsManager.notifyAllAssigned(it)
            }

            SERVICE_TO_ACTIVITY, ACTIVITY_COLD -> DaemonsManager.notifyAllAssigned(activity)
            else -> {}
        }
    }

    suspend fun hideFragment() {
        ready.postValue(true)

        //Delay so fragment does not crash and animation runs smoothly
        delay(100)

        fm.popBackstack(false, fadeLong)
    }
}