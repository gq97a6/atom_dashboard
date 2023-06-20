package com.alteratom.dashboard.activities

import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.objects.ActivityHandler
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.initializeGlobals
import com.alteratom.dashboard.objects.Storage.saveToFile
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    var doOverrideOnBackPress: () -> Boolean = { false }
    private var hasBooted = false
    private var hasShutdown = false

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        boot()
    }

    override fun onStart() {
        super.onStart()
        if (!hasBooted) boot()
    }

    override fun onResume() {
        super.onResume()
        if (!hasBooted) boot()
    }

    override fun onPause() {
        super.onPause()
        if (!hasShutdown) shutdown()
    }

    override fun onStop() {
        super.onStop()
        if (!hasShutdown) shutdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!hasShutdown) shutdown()
    }

    private fun boot() {
        hasShutdown = false

        ForegroundService.service?.finishAffinity = { finishAffinity() }

        //Partially initialize globals if service has not been started
        if (ForegroundService.service?.isStarted != true) initializeGlobals(0)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this, false)

        //Setup fragment manager and switchers
        TileSwitcher.activity = this
        FragmentSwitcher.activity = this
        fm = FragmentManager(this)
        fm.replaceWith(SetupFragment(), false, null)

        onBackPressedDispatcher.addCallback(this) {
            if (!doOverrideOnBackPress() && !fm.popBackStack()) finish()
        }

        hasBooted = true
    }

    private fun shutdown() {
        hasBooted = false

        G.dashboards.saveToFile()
        G.settings.saveToFile()
        G.theme.saveToFile()

        hasShutdown = true
    }

    //
}
