package com.alteratom.dashboard.helper_objects

import ButtonTile
import android.app.Application
import android.content.Context
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.app.AtomApp.Companion.aps
import com.alteratom.dashboard.checkBilling
import com.alteratom.dashboard.daemon.DaemonsManager
import com.alteratom.dashboard.isBatteryOptimized
import com.alteratom.dashboard.helper_objects.FragmentManager.fm
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.ACTIVITY
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.ACTIVITY_COLD
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.ACTIVITY_TO_SERVICE
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.SERVICE
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.SERVICE_COLD
import com.alteratom.dashboard.helper_objects.Setup.SetupCase.SERVICE_TO_ACTIVITY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//Sorted by order of execution setup sequence
object Setup {
    enum class SetupCase {
        //------------------------------------------------------------
        //Cases when background work is enabled
        //------------------------------------------------------------
        SERVICE, //When app starts WITH service running
        SERVICE_COLD, //When app starts WITHOUT service running
        ACTIVITY_TO_SERVICE, //When app starts WITH activity running

        //------------------------------------------------------------
        //Cases when background work is disabled
        //------------------------------------------------------------
        ACTIVITY, //When app starts WITH activity running
        ACTIVITY_COLD, //When app starts WITHOUT activity running
        SERVICE_TO_ACTIVITY, //When app starts WITH service running
    }

    fun restart(app: Application) {
        //TODO: Clear up here
        fm.backstack = mutableListOf()
        initialize(app)
    }

    fun initialize(app: Application) {
        setFilesPaths(app)
        initializeBasicGlobals()

        //Run rest in non-blocking way
        CoroutineScope(Dispatchers.Default).launch {
            updateProStatus()
            checkBilling(app)
            checkBatteryStatus(app)
            val case = getSetupCase()
            configureForegroundService(app, case)
            initializeOtherGlobals()
            assignDaemons(app, case)
        }
    }

    private fun setFilesPaths(context: Context) {
        Debug.log("SETUP_PATHS")
        aps.rootFolder = context.filesDir.canonicalPath.toString()
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
            aps.tile = ButtonTile().apply {  }
            aps.theme = Storage.parseSave() ?: Theme()
            aps.settings = Storage.parseSave() ?: Settings()
        }
    }

    private fun updateProStatus() {
        aps.isLicensed = Pro.getLicenceStatus()
    }

    private fun checkBilling(context: Context) = context.checkBilling()

    //Check if battery optimization is enabled
    private fun checkBatteryStatus(context: Context) {
        //Disable foreground service if battery is optimized
        if (context.isBatteryOptimized()) aps.settings.fgEnabled = false
    }

    //Set setup case
    private fun getSetupCase(): SetupCase {
        val case = if (ForegroundService.service?.isStarted == true) {
            if (aps.settings.fgEnabled) SERVICE
            else SERVICE_TO_ACTIVITY
        } else {
            if (aps.settings.fgEnabled) {
                if (aps.isInitialized.value == true) ACTIVITY_TO_SERVICE
                else SERVICE_COLD
            } else {
                if (aps.isInitialized.value == true) ACTIVITY
                else ACTIVITY_COLD
            }
        }

        Debug.log("SETUP-CASE_${case.name}")
        return case
    }

    //Either stop foreground service if it is no longer used or set it up
    private suspend fun configureForegroundService(context: Context, case: SetupCase) {
        when (case) {
            SERVICE_TO_ACTIVITY -> {
                //Discharge all daemons
                DaemonsManager.notifyAllDischarged()
                ForegroundService.stop(context)
            }

            ACTIVITY_TO_SERVICE, SERVICE_COLD -> {
                //Discharge all daemons
                DaemonsManager.notifyAllDischarged()

                //Foreground service disabled by settings or battery usage is optimised
                ForegroundService.start(context)
                ForegroundService.haltForService()

                //Configure service
                ForegroundService.service?.finishAndRemoveTask = { /* TODO: KILL WHOLE APP HERE */ }
            }

            else -> {}
        }
    }

    //Setup required global variables
    private fun initializeOtherGlobals() {
        if (aps.isInitialized.value == false) {
            aps.dashboards = Storage.parseListSave()
            aps.isInitialized.postValue(true)
        }
    }

    //Assign all daemons either to foreground service or application context
    private fun assignDaemons(context: Context, case: SetupCase) {
        when (case) {
            SERVICE_COLD, ACTIVITY_TO_SERVICE -> ForegroundService.service?.let {
                DaemonsManager.notifyAllAssigned(it)
            }

            SERVICE_TO_ACTIVITY, ACTIVITY_COLD -> DaemonsManager.notifyAllAssigned(context)
            else -> {}
        }
    }
}