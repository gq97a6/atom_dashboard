package com.netDashboard.tile

import android.graphics.Color
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.getScreenWidth
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.recycler_view.RecyclerViewElement
import org.eclipse.paho.client.mqttv3.MqttMessage

abstract class Tile : RecyclerViewElement() {

    var color = Color.parseColor("#BF4040")
    var isColouredByTheme = false

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

    val type = this.javaClass.toString()

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((getScreenWidth() - view.paddingLeft * 2) / (adapter?.spanCount ?: 1)) * height
        view.layoutParams = params
    }

    @Transient
    var name = ""

    @Transient
    var dashboardName: String = ""

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

    open fun setThemeColor(color: Int) {
        this.color = color
    }

    open fun onSend(topic: String, msg: String, qos: Int, retained: Boolean = false): Boolean {
        Dashboards.get(dashboardName).daemonGroup?.mqttd.let {
            return if (it != null) {
                it.publish(topic, msg, qos, retained)
                true
            } else {
                false
            }
        }
    }

    open fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!mqttEnabled) return false
        if (!mqttTopics.subs.topicList.contains(data.first)) return false
        return true
    }
}