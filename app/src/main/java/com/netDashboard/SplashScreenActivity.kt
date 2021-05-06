package com.netDashboard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.netDashboard.dashboard_activity.DashboardActivity
import com.netDashboard.databinding.SplashScreenActivityBinding
import com.netDashboard.main_activity.MainActivity
import java.util.*
import kotlin.concurrent.schedule

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var b: SplashScreenActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = SplashScreenActivityBinding.inflate(layoutInflater)
        setContentView(b.root)

        val con = this
        Timer().schedule(500) {
            Intent(con, MainActivity::class.java).also {
                finish()
                startActivity(it)
            }
        }
    }
}
