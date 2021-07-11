package com.netDashboard.tile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netDashboard.R
import com.netDashboard.alpha
import com.netDashboard.dashboard.Dashboards
import com.netDashboard.getScreenWidth
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

open class Tile {

    var width = 1
    var height = 1
    var spanCount = 1

    var color = Color.parseColor("#BF4040")
    var isColouredByTheme = false

    var mqttEnabled = true
    var mqttTopics = MqttTopics()
    var mqttPubValue = ""
    var mqttQoS = 0
    var mqttPubConfirmation = false
    var mqttPayloadJSON = false

    var bltPattern = ""
    var bltDelimiter = ""
    var bltRequestToGet = ""
    var bltPayloadJSON = false
    var bltOutputJSON = ""

    val id: Long?
    val type = this.javaClass.toString()

    @Transient
    open val layout = 0

    @Transient
    var name = ""

    @Transient
    var context: Context? = null

    @Transient
    var holder: TilesAdapter.TileViewHolder? = null

    @Transient
    var dashboardName: String = ""

    @Transient
    var flag = ""
        private set

    @Transient
    var isEdit = false
        set(value) {
            field = value; onEdit(value)
        }

    init {
        id = Random().nextLong()
    }

    fun getItemViewType(context: Context, spanCount: Int): Int {
        this.context = context
        this.spanCount = spanCount

        return layout
    }

    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TilesAdapter.TileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)

        return TilesAdapter.TileViewHolder(view)
    }

    open fun onBindViewHolder(holder: TilesAdapter.TileViewHolder, position: Int) {
        this.holder = holder

        val view = holder.itemView
        val params = view.layoutParams

        params.height = ((getScreenWidth() - view.paddingLeft * 2) / spanCount) * height
        view.layoutParams = params

        onEdit(isEdit)
        flag(flag)
    }

    fun areItemsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem == newItem
    }

    fun areContentsTheSame(oldItem: Tile, newItem: Tile): Boolean {
        return oldItem.id == newItem.id
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

    fun toggleFlag(flag: String) {
        if (this.flag.isNotEmpty()) {
            flag("")
        } else {
            flag(flag)
        }
    }

    fun flag(flag: String = "") {
        this.flag = flag

        val flagMark = holder?.itemView?.findViewById<View>(R.id.flag_mark)
        val flagBackground = holder?.itemView?.findViewById<View>(R.id.flag_background)

        when (flag) {
            "swap" -> flagMark?.setBackgroundResource(R.drawable.icon_swap_flag)
            "remove" -> flagMark?.setBackgroundResource(R.drawable.icon_remove_flag)
            "lock" -> flagMark?.setBackgroundResource(R.drawable.icon_lock_flag)
        }

        if (flag.isNotEmpty()) {
            flagMark?.backgroundTintList = ColorStateList.valueOf(-16777216)
            flagBackground?.setBackgroundColor((-1).alpha(.7f))

            flagMark?.visibility = View.VISIBLE
            flagBackground?.visibility = View.VISIBLE
        } else {
            flagMark?.visibility = View.GONE
            flagBackground?.visibility = View.GONE
        }
    }

    open fun setThemeColor(color: Int) {
        this.color = color
    }

    open fun onClick() {}

    open fun onLongClick() {}

    open fun onEdit(isEdit: Boolean) {}

    open fun onSend(topic: String, msg: String, qos: Int, retained: Boolean = false): Boolean {
        Dashboards.get(dashboardName)?.daemonGroup?.mqttd.let {
            return if (it != null) {
                it.publish(topic, msg)
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