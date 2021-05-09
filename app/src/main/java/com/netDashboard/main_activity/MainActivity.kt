package com.netDashboard.main_activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.netDashboard.config_new_tile_activity.ConfigNewTileActivity
import com.netDashboard.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.MainActivityBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var b: MainActivityBinding
    private val counter = MutableLiveData(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = MainActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        Handler(Looper.getMainLooper()).postDelayed({
            Intent(this, DashboardActivity::class.java).also {
                it.putExtra("dashboardName", "main")
                finish()
                startActivity(it)
            }
        }, 3300)

        counter.observe(this, {
            b.counter.text = it.toString()
        })

        thread {
            while(counter.value!! > 1) {
                Thread.sleep(1000)
                counter.postValue(counter.value!! - 1)
            }


        }
    }
}
