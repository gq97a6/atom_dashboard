package com.netDashboard.tile

import com.netDashboard.dashboard.Dashboard
import com.netDashboard.recycler_view.BaseRecyclerViewAdapter
import com.netDashboard.recycler_view.BaseRecyclerViewItem
import com.netDashboard.screenWidth
import org.eclipse.paho.client.mqttv3.MqttMessage

@Suppress("UNUSED")
abstract class Tile : BaseRecyclerViewItem() {

    @Transient
    var dashboard: Dashboard = Dashboard("err")
    override val adapterTheme
        get() = dashboard.resultTheme

    val type = this.javaClass.toString()
    abstract var typeTag: String

    var mqttEnabled = true
    var mqttTopics = MqttTopics()
    abstract val mqttDefaultPubValue: String
    abstract var mqttPubValue: String
    var mqttQoS = 0
    var mqttPubConfirm = false
    var mqttPayloadIsJSON = false

    var bltPattern = ""
    var bltDelimiter = ""
    var bltRequestToGet = ""
    var bltPayloadJSON = false
    var bltOutputJSON = ""

    companion object {
        fun MutableList<Tile>.byId(id: Long): Tile? =
            this.find { it.id == id }
    }

    override fun onBindViewHolder(holder: BaseRecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((screenWidth - view.paddingLeft * 2) / (adapter?.spanCount ?: 1)) * height
        view.layoutParams = params

        applyTheme()
    }

    class MqttTopics {
        val subs = TopicList()
        val pubs = TopicList()

        class TopicList {

            val topics: MutableList<Topic>
                get() = topicMap.values.toMutableList()

            val topicList: MutableList<String>
                get() = topicListMap.values.toMutableList()

            private val topicMap: MutableMap<String, Topic> = mutableMapOf()
            private val topicListMap: MutableMap<String, String> = mutableMapOf()

            fun get(name: String): Topic = topicMap[name] ?: Topic("", 0, false)

            fun set(topic: String?, qos: Int?, retained: Boolean?, name: String) {
                val t = topicMap[name] ?: Topic("", 0, false)

                if (retained != null) t.retained = retained
                if (qos != null) t.qos = qos
                if (topic != null) t.topic = topic

                topicMap[name] = t
                topicListMap[name] = t.topic
            }

            data class Topic(var topic: String, private val _qos: Int, var retained: Boolean) {
                var qos = _qos
                    set(value) {
                        if (value in 0..3) field = value
                    }
            }
        }
    }

    open fun onSend(topic: String, msg: String, qos: Int, retained: Boolean = false): Boolean {
        dashboard.daemonGroup?.mqttd?.let {
            it.publish(topic, msg, qos, retained)
            return true
        }
        return false
    }

    open fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!mqttEnabled) return false
        if (!mqttTopics.subs.topicList.contains(data.first)) return false
        return true
    }
}