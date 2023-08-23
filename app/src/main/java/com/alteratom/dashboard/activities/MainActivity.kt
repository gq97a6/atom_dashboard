package com.alteratom.dashboard.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.activities.fragments.DashboardFragment
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.hasBooted
import com.alteratom.dashboard.objects.G.hasShutdown
import com.alteratom.dashboard.objects.G.initializeGlobals
import com.alteratom.dashboard.objects.Storage.saveToFile
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    var doOverrideOnBackPress: () -> Boolean = { false }

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        boot()
    }

    //Might be called without onDestroy when the app closes
    override fun onStop() {
        super.onStop()
        if (!hasShutdown) shutdown()
    }

    //Might be called directly without onStop
    override fun onDestroy() {
        super.onDestroy()
        if (!hasShutdown) shutdown()
    }

    private fun boot() {
        Log.i("ALTER_ATOM", "BOOT")

        hasShutdown = false

        //Partially initialize globals if service has not been started
        if (ForegroundService.service?.isStarted != true) initializeGlobals(0)

        //Apply theme
        G.theme.apply(b.root, this, false)

        //Setup fragment manager and start setup fragment
        fm = FragmentManager(this)
        fm.replaceWith(SetupFragment(), false, null)

        hasBooted = true
    }

    private fun shutdown() {
        Log.i("ALTER_ATOM", "SHUTDOWN")

        hasBooted = false

        G.dashboards.saveToFile()
        G.settings.saveToFile()
        G.theme.saveToFile()

        hasShutdown = true
    }
}
