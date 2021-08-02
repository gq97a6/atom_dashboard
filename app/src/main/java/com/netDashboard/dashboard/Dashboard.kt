package com.netDashboard.dashboard

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import com.netDashboard.R
import com.netDashboard.activities.dashboard.DashboardActivity
import com.netDashboard.foreground_service.DaemonGroup
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewElement
import com.netDashboard.settings.Settings
import com.netDashboard.tile.Tile
import java.util.*

open class Dashboard(val name: String) : RecyclerViewElement() {

    @Transient
    override val layout = R.layout.dashboard_list_element

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerViewAdapter.ViewHolder {
        super.onCreateViewHolder(parent, viewType)

        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        view.findViewById<Button>(R.id.dle_button).text = name.uppercase(Locale.getDefault())

        view.findViewById<Button>(R.id.dle_button).setOnClickListener {

            Intent(adapter?.context, DashboardActivity::class.java).also {
                Settings.lastDashboardName = name

                it.putExtra("dashboardName", name)
                (adapter?.context as Activity).overridePendingTransition(0, 0)
                (adapter?.context as Activity).startActivity(it)
                (adapter?.context as Activity).finish()
            }
        }

        return RecyclerViewAdapter.ViewHolder(view)
    }
}