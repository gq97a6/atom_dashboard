package com.alteratom.dashboard.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.foreground_service.ForegroundService
import com.alteratom.dashboard.objects.ActivityHandler
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.G.initializeGlobals
import com.alteratom.dashboard.objects.G.rootFolder
import com.alteratom.dashboard.objects.Storage
import com.alteratom.dashboard.switcher.FragmentSwitcher
import com.alteratom.dashboard.switcher.TileSwitcher
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    //TODO
    var onBackPressedBoolean: () -> Boolean = { false }

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityHandler.onCreate(this)

        //Partially initialize globals if service has not been started
        if (ForegroundService.service?.isStarted != true) initializeGlobals(1)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this, false)

        //Setup fragment manager and switchers
        fm = FragmentManager(this)
        fm.replaceWith(SetupFragment(), false, null)
        TileSwitcher.activity = this
        FragmentSwitcher.activity = this
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityHandler.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        ActivityHandler.onPause()
    }

    override fun onBackPressed() {
        if (!onBackPressedBoolean()) {
            if (!fm.popBackStack()) super.onBackPressed()
        }
    }
}
