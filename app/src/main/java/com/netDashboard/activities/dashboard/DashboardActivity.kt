package com.netDashboard.activities.dashboard

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent.ACTION_DOWN
import android.view.KeyEvent.ACTION_UP
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.netDashboard.*
import com.netDashboard.activities.MainActivity
import com.netDashboard.activities.dashboard.properties.DashboardPropertiesActivity
import com.netDashboard.activities.dashboard.tile_new.TileNewActivity
import com.netDashboard.activities.dashboard.tile_properties.TilePropertiesActivity
import com.netDashboard.app_on.AppOn
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.dashboard.Dashboard.Companion.byId
import com.netDashboard.databinding.ActivityDashboardBinding
import com.netDashboard.globals.G
import com.netDashboard.globals.G.dashboards
import com.netDashboard.log.LogAdapter
import com.netDashboard.tile.TilesAdapter
import com.netDashboard.toolbarControl.ToolBarController
import java.util.*

@SuppressLint("ClickableViewAccessibility")
class DashboardActivity : AppCompatActivity() {
    private lateinit var b: ActivityDashboardBinding

    //private lateinit var dashboard: Dashboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AppOn.create(this)

        //dashboard = dashboards.byId(intent.getLongExtra("dashboardId", 0))
        //if (dashboard.isInvalid) Intent(this, MainActivity::class.java).also {
        //    startActivity(it)
        //}

        b = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(b.root)
    }
}