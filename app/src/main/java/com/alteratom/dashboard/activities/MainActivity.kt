package com.alteratom.dashboard.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.objects.ActivityHandler
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.Storage.rootFolder
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.foreground_service.ForegroundService
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

        //BOOT_A - initialize globals
        if (ForegroundService.service?.isStarted != true || G.dashboards.isEmpty()) {
            rootFolder = filesDir.canonicalPath.toString()
            G.initialize()
        }

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        G.theme.apply(b.root, this, false)

        //BOOT_B - setup
        fm = FragmentManager(this)
        fm.replaceWith(SetupFragment(), false, null)
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
