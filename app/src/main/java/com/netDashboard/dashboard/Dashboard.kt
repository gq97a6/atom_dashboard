package com.netDashboard.dashboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.settings.Settings
import com.netDashboard.tile.Tile
import java.util.*

open class Dashboard(val name: String) {

    val id: Long?

    @Transient
    var context: Context? = null

    @Transient
    private var holder: DashboardAdapter.DashboardsViewHolder? = null

    @Transient
    var daemonGroup: DaemonGroup? = null

    @Transient
    var tiles: MutableList<Tile> = mutableListOf()
        set(value) {
            for (t in value) t.dashboardName = name
            field = value
        }

    var dashboardTagName = name

    var spanCount = 3

    var mqttEnabled: Boolean = false
    var mqttAddress = "tcp://"
    var mqttPort = 1883
    val mqttURI
        get() = "$mqttAddress:$mqttPort"

    var bluetoothEnabled: Boolean = false

    init {
        id = Random().nextLong()
    }

    fun getItemViewType(context: Context): Int {
        this.context = context

        return R.layout.dashboard_list_element
    }

    open fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardAdapter.DashboardsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        view.findViewById<Button>(R.id.dle_button).text = name.uppercase(Locale.getDefault())

        view.findViewById<Button>(R.id.dle_button).setOnClickListener {

            Intent(context, DashboardActivity::class.java).also {
                Settings.lastDashboardName = name

                it.putExtra("dashboardName", name)
                (context as Activity).overridePendingTransition(0, 0)
                context?.startActivity(it)
            }
        }
        return DashboardAdapter.DashboardsViewHolder(view)
    }

    open fun onBindViewHolder(holder: DashboardAdapter.DashboardsViewHolder, position: Int) {
        this.holder = holder
    }

    fun areItemsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Dashboard, newItem: Dashboard): Boolean {
        return oldItem.id == newItem.id
    }
}