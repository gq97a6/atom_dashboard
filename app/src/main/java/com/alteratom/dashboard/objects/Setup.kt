package com.alteratom.dashboard.objects

import androidx.activity.addCallback
import com.alteratom.dashboard.BillingHandler.Companion.checkBilling
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.activities.MainActivity.Companion.fm
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.activities.fragments.SetupFragment.Companion.ready
import com.alteratom.dashboard.areNotificationsAllowed
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.requestNotifications
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import kotlinx.coroutines.delay

//Sorted by order of execution setup sequence
object Setup {

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
        fm = FragmentManager(activity)

        activity.apply {
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

    suspend fun service(activity: MainActivity) {
        //Discharge all daemons
        DaemonsManager.notifyAllDischarged()

        if (!G.settings.fgEnabled && ForegroundService.service?.isStarted == true) {
            //Foreground service enabled by settings and battery usage is not optimised
            ForegroundService.stop(activity)
        } else if (G.settings.fgEnabled && ForegroundService.service?.isStarted != true) {
            //Foreground service disabled by settings or battery usage is optimised
            ForegroundService.start(activity)
            ForegroundService.haltForService()

            //Configure service
            ForegroundService.service?.finishAndRemoveTask = { activity.finishAndRemoveTask() }
        }
    }

    fun globals() {
        if (!G.areInitialized) {
            G.dashboards = Storage.parseListSave()
            G.areInitialized = true
        }
    }

    fun permissions(activity: MainActivity) {
        activity.apply { if (!areNotificationsAllowed()) requestNotifications() }
    }

    fun daemons(activity: MainActivity) {
        if (!G.settings.fgEnabled) DaemonsManager.notifyAllAssigned(activity)
        else ForegroundService.service?.let { DaemonsManager.notifyAllAssigned(it) }
    }

    suspend fun hideFragment() {
        ready.postValue(true)

        //Delay so fragment does not crash and animation runs smoothly
        delay(100)

        fm.popBackstack(false, fadeLong)
    }
}