package com.alteratom.dashboard.objects

import android.app.Activity
import android.content.Context
import android.util.Log
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
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.objects.Storage.saveToFile
import com.alteratom.dashboard.restart
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Setup {
    fun setupPaths(activity: MainActivity) {
        if (G.rootFolder != "") return

        G.rootFolder = activity.filesDir.canonicalPath.toString()
        G.path = mapOf(
            Theme::class to "${G.rootFolder}/theme",
            Settings::class to "${G.rootFolder}/settings",
            Dashboard::class to "${G.rootFolder}/dashboards"
        )
    }

    fun setupProStatus() = Pro.updateStatus()

    fun setupBilling(activity: MainActivity) = activity.checkBilling()

    fun setupBasicGlobals() {
        if (ForegroundService.service?.isStarted != true) {
            G.theme = Storage.parseSave() ?: Theme()
            G.settings = Storage.parseSave() ?: Settings()
        }
    }

    fun setupFragmentManager(activity: MainActivity) {
        fm = FragmentManager(activity)
    }

    private fun shutdown() {
        G.dashboards.saveToFile()
        G.settings.saveToFile()
        G.theme.saveToFile()
    }

    fun onStart(activity: MainActivity) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.i("ALTER_ATOM", "SETUP_FRAGMENT")

            //LEAVE IT THERE
            delay(50)

            activity.apply {
                ForegroundService.service?.finishAndRemoveTask = { finishAndRemoveTask() }

                //Setup switchers
                TileSwitcher.activity = this
                FragmentSwitcher.activity = this

                //Setup on back press callback
                runOnUiThread {
                    onBackPressedDispatcher.addCallback {
                        if (!fm.doOverrideOnBackPress() && !fm.popBackstack()) finishAndRemoveTask()
                    }
                }
            }

            //Disable foreground service if battery is optimized
            if (activity.isBatteryOptimized()) G.settings.fgEnabled = false

            //Foreground service enabled by settings and battery usage is not optimised
            if (G.settings.fgEnabled) foregroundServiceAllowed(activity)
            else foregroundServiceDisallowed(activity)
            //Foreground service disabled by settings or battery usage is optimised
        }
    }

    private fun foregroundServiceDisallowed(activity: MainActivity) {
        //Disable foreground service as it should be
        if (ForegroundService.service?.isStarted == true) ForegroundService.stop(activity)

        //Initialize globals with activity as context if not already
        if (!G.areInitialized) {
            G.dashboards = Storage.parseListSave()
            G.areInitialized = true

            if (G.settings.startFromLast && G.setCurrentDashboard(G.settings.lastDashboardId)) {
                fm.addBackstack(DashboardFragment())
            }
        }

        DaemonsManager.notifyAllRemoved()
        DaemonsManager.notifyAllAdded(activity)

        onSetupDone()
    }

    private suspend fun foregroundServiceAllowed(activity: MainActivity) {

        if (ForegroundService.service?.isStarted == true) activity.apply { //Service already launched

            if (G.areInitialized) onSetupDone()
            else { //Something went wrong | Globals should be initialized as service is

                Log.i("ALTER_ATOM", "NOT_INITIALIZED_ERROR")

                //Stop foreground service
                ForegroundService.stop(this@apply)

                //Restart in three seconds
                delay(1000)
                restart("STARTUP_ERROR_01")
            }
        } else activity.apply { //Service not launched

            //Start foreground service
            ForegroundService.start(this@apply)

            //Wait for service
            if (ForegroundService.haltForService() == null) restart("STARTUP_ERROR_02") //restart if failed
            else {
                //Configure service
                ForegroundService.service?.finishAndRemoveTask = { finishAndRemoveTask() }

                //Initialize globals with service as context if not already
                if (!G.areInitialized) {
                    G.dashboards = Storage.parseListSave()
                    G.areInitialized = true
                }

                DaemonsManager.notifyAllRemoved()
                ForegroundService.service?.let { DaemonsManager.notifyAllAdded(it) }

                onSetupDone()
            }
        }
    }

    private fun onSetupDone() {
        //ready.postValue(true)
        fm.popBackstack(false, fadeLong)
    }
}