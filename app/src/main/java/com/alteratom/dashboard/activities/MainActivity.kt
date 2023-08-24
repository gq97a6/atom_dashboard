package com.alteratom.dashboard.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.alteratom.dashboard.FragmentManager
import com.alteratom.dashboard.Settings
import com.alteratom.dashboard.Theme
import com.alteratom.dashboard.activities.fragments.SetupFragment
import com.alteratom.dashboard.objects.G
import com.alteratom.dashboard.objects.Setup.setupPaths
import com.alteratom.dashboard.objects.Storage
import com.alteratom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var b: ActivityMainBinding

    companion object {
        lateinit var fm: FragmentManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupPaths(this)

        //Test
        G.theme = Storage.parseSave() ?: Theme()
        G.settings = Storage.parseSave() ?: Settings()

        //Apply theme
        G.theme.apply(b.root, this, false)

        fm = FragmentManager(this)
        fm.replaceWith(SetupFragment(), false, null)

        Log.i("ALTER_ATOM", "TEEEEEEEEST")
    }

    //Might be called without onDestroy when the app closes
    override fun onStop() {
        super.onStop()
    }

    //Might be called directly without onStop
    override fun onDestroy() {
        super.onDestroy()
    }
}
