package com.alteratom.dashboard.helper_objects

import ButtonTile
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.ForegroundService.Companion.service
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activity.MainActivity.Companion.fm
import com.alteratom.dashboard.app.AtomApp.Companion.app
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.checkBilling
import com.alteratom.dashboard.createToast
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.fragment.LoadingFragment
import com.alteratom.dashboard.fragment.MainScreenFragment
import com.alteratom.dashboard.fragment.SettingsFragment
import com.alteratom.dashboard.helper_objects.Storage.saveToFile
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.manager.FragmentManager.Animations.fadeLong
import com.alteratom.dashboard.observeUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//Sorted by order of execution setup sequence
object Setup {
    fun applyConfig(dashboards: MutableList<Dashboard>, settings: Settings, theme: Theme) {
        CoroutineScope(Dispatchers.Main).launch {
            //Reset app status initialization flag
            aps.isInitialized = MutableLiveData(false)

            //Launch loading fragment
            fm.replaceWith(LoadingFragment(), animation = null)

            //Discharge all current daemons
            DaemonsManager.notifyAllDischarged()

            //Apply backup files
            dashboards.saveToFile()
            settings.saveToFile()
            theme.saveToFile()

            //Reset backstack
            fm.backstack = mutableListOf(MainScreenFragment())

            aps.isInitialized.observeUntil {
                if (it != true) return@observeUntil false

                //Replace with settings fragment
                fm.replaceWith(SettingsFragment(), false, fadeLong)

                //Remove observer
                return@observeUntil true
            }

            //Initialize the app
            initialize()
        }

    }

    fun initialize() {
        Debug.log("INIT")
        setFilesPaths()
        initializeBasicGlobals()

        //Run rest in non-blocking way
        CoroutineScope(Dispatchers.Default).launch {
            updateProStatus()
            checkBilling()
            checkBatteryStatus()
            configureForegroundService()
            initializeOtherGlobals()
            assignDaemons()
            finish()
        }
    }

    private fun setFilesPaths() {
        Debug.log("SETUP_PATHS")
        aps.rootFolder = app.filesDir.canonicalPath.toString()
        aps.path = mapOf(
            Theme::class to "${aps.rootFolder}/theme",
            Settings::class to "${aps.rootFolder}/settings",
            Dashboard::class to "${aps.rootFolder}/dashboards"
        )
    }

    private fun initializeBasicGlobals() {
        if (aps.isInitialized.value == false) {
            Debug.log("SETUP_BASIC_GLOBALS")
            aps.dashboard = Dashboard(name = "Error")
            aps.tile = ButtonTile().apply { tag = "Error" }
            aps.theme = Storage.parseSave() ?: Theme()
            aps.settings = Storage.parseSave() ?: Settings()
        }
    }

    private fun updateProStatus() {
        aps.isLicensed = Pro.getLicenceStatus()
    }

    private fun checkBilling() = app.checkBilling()

    //Check if battery optimization is enabled
    private fun checkBatteryStatus() {
        //Disable foreground service if battery is optimized
        if (app.isBatteryOptimized()) {
            aps.settings.fgEnabled = false
            createToast(app, "Disabling background work due to battery optimization")
        }
    }

    //Either stop foreground service if it is no longer used or set it up
    private suspend fun configureForegroundService() {

        //Service not enabled but is running
        if (!aps.settings.fgEnabled && service?.isStarted == true) {
            DaemonsManager.notifyAllDischarged()
            ForegroundService.stop(app)
        }

        //Service enabled but has not started
        else if (aps.settings.fgEnabled && service?.isStarted != true) {
            //Discharge all daemons in case any is running
            DaemonsManager.notifyAllDischarged()

            //Foreground service disabled by settings or battery usage is optimised
            ForegroundService.start(app)
            ForegroundService.haltForService()

            //Configure service
            service?.finishAndRemoveTask = { /* TODO: KILL WHOLE APP HERE */ }
        }
    }

    //Setup required global variables
    private fun initializeOtherGlobals() {
        if (aps.isInitialized.value == false) {
            aps.dashboards = Storage.parseListSave()
        }
    }

    //Assign all daemons either to foreground service or application context
    private fun assignDaemons() {
        val context: Context = if (!aps.settings.fgEnabled) app
        else service!!

        DaemonsManager.notifyAllAssigned(context)
    }

    private fun finish() = aps.isInitialized.postValue(true)
}