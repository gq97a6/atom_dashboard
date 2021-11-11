package com.netDashboard.tile

import androidx.annotation.IntRange
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("UNUSED")
abstract class Tile : BaseRecyclerViewItem() {

    @Transient
    var dashboard: Dashboard = Dashboard("err")

    val type = this.javaClass.toString()
    abstract var typeTag: String

    abstract val mqttData: MqttData

    //var bltPattern = ""
    //var bltDelimiter = ""
    //var bltRequestToGet = ""
    //var bltPayloadJSON = false
    //var bltOutputJSON = ""

    companion object {
        fun MutableList<Tile>.byId(id: Long): Tile? =
            this.find { it.id == id }
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((screenWidth - view.paddingLeft * 2) / (adapter.spanCount)) * height
        view.layoutParams = params
    }

    class MqttData(defaultPubValue: String) {
        var isEnabled = true
        var lastReceive = Date()

        val subs: MutableMap<String, String> = mutableMapOf()
        val pubs: MutableMap<String, String> = mutableMapOf()

        var qos = 0
            set(value) {
                field = minOf(2, maxOf(0, value))
            }

        var pubValue = defaultPubValue
        var confirmPub = false
        var payloadIsJSON = false
    }

    open fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ): Boolean {
        if (topic.isNullOrEmpty()) return false

        dashboard.daemonGroup?.mqttd?.let {
            it.publish(topic, msg, qos, retained)
            return true
        }

        return false
    }

    open fun onSend(
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ): Boolean = onSend(mqttData.pubs["base"], msg, qos, retained)

    open fun onReceive(data: Pair<String?, MqttMessage?>): Boolean {
        if (!mqttData.isEnabled) return false
        if (!mqttData.subs.containsValue(data.first)) return false

        return true
    }
}