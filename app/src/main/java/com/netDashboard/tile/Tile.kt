package com.netDashboard.tile

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netDashboard.R
import com.netDashboard.alpha
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
    var mqttPubConfirmation = false
    var mqttQoS = 0
    var mqttPayloadJSON = false
    var mqttOutputJSON = false

    var bltPattern = ""
    var bltDelimiter = ""
    var bltRequestToGet = ""
    var bltPayloadJSON = false
    var bltOutputJSON = ""

    val id: Long?

    val type = this.javaClass.toString()

    open var layout = 0

    @Transient
    var name = ""

    @Transient
    var context: Context? = null

    @Transient
    var holder: TilesAdapter.TileViewHolder? = null

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
        val sub = List()
        val pub = List()

        class List {
            private val topics: MutableList<String> = mutableListOf()
            private val names: MutableList<String> = mutableListOf()

            fun set(topic: String, name: String) {
                for ((i, n) in names.withIndex()) {
                    if (n == name) {
                        topics[i] = topic
                        return
                    }
                }

                topics.add(topic)
                names.add(name)
            }

            fun get(name: String): String? {
                for ((i, n) in names.withIndex()) {
                    if (n == name) return topics[i]
                }

                return null
            }

            fun get(): MutableList<String> {
                return topics
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

    open fun onSend(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {}

    fun onSend(topic: String, msg: String, retained: Boolean = false) {
        onSend(topic, msg, 1, retained)
    }

    open fun onData(data: Pair<String?, MqttMessage?>): Boolean {
        if (!mqttEnabled) return false
        if (!mqttTopics.sub.get().contains(data.first)) return false
        return true
    }
}