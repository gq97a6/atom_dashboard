package com.alteratom.dashboard.tile

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.IntRange
import com.alteratom.R
import com.alteratom.dashboard.*
import com.alteratom.dashboard.DialogBuilder.buildConfirm
import com.alteratom.dashboard.FolderTree.mapper
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.activities.MainActivity
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("UNUSED")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class Tile : RecyclerViewItem() {

    @JsonIgnore
    var dashboard: Dashboard? = null

    @JsonIgnore
    open var height = 1f

    var tag = ""
    abstract var typeTag: String

    abstract var iconKey: String
    val iconRes: Int
        get() = Icons.icons[iconKey]?.res ?: R.drawable.il_interface_plus

    var hsv = theme.a.hsv.let {
        floatArrayOf(it[0], it[1], it[2])
    }

    val pallet: ColorPallet
        get() = theme.a.getColorPallet(hsv, true)

    val mqtt = MqttClientData()

    var doLog = false
    var doNotify = false
    var silentNotify = false

    companion object {
        fun MutableList<Tile>.byId(id: Long): Tile? =
            this.find { it.id == id }
    }

    open fun onCreateTile() {}

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)
        performClick(adapter.context)
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val view = holder.itemView
        val params = view.layoutParams

        params.height =
            ((screenWidth - view.paddingLeft * 2) * height / 1.61803398875 / adapter.spanCount).toInt()
        view.layoutParams = params

        holder.itemView.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconRes)
        holder.itemView.findViewById<TextView>(R.id.t_tag)?.text = tag.ifBlank { "???" }
    }

    open fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = pallet
        )
    }

    open fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ) {
    }

    open fun onReceive(data: Pair<String?, MqttMessage?>, jsonResult: MutableMap<String, String>) {
    }

    fun Mqttd.send(
        msg: String,
        topic: String? = mqtt.pubs["base"],
        @IntRange(from = 0, to = 2) qos: Int = mqtt.qos,
        retain: Boolean = false,
        raw: Boolean = false
    ) {
        if (topic.isNullOrEmpty()) return

        {
            this.publish(topic, msg, qos, retain)
            onSend(topic, msg, qos, retain)
        }.let {
            if (!mqtt.doConfirmPub || raw) it()
            else with(adapter.context as MainActivity) {
                buildConfirm("Confirm publishing", "PUBLISH", { it() })
            }
        }
    }

    fun send(
        msg: String,
        topic: String? = mqtt.pubs["base"],
        @IntRange(from = 0, to = 2) qos: Int = mqtt.qos,
        retain: Boolean = false,
        raw: Boolean = false
    ) {
        when (dashboard?.daemon) {
            is Mqttd -> (dashboard?.daemon as? Mqttd?)?.send(msg, topic, qos, retain, raw)
        }
    }

    fun receive(data: Pair<String?, MqttMessage?>) {
        if (!mqtt.isEnabled) return
        if (!mqtt.subs.containsValue(data.first)) return

        //Build map of jsonPath key and value. Null on absence or fail.
        val jsonResult = mutableMapOf<String, String>()
        if (mqtt.payloadIsJson) {
            for (p in mqtt.jsonPaths) {
                data.second.toString().let {
                    try {
                        mapper.readTree(it).at(p.value).asText()
                    } catch (e: Exception) {
                        null
                    }?.let {
                        jsonResult[p.key] = it
                    }
                }
            }
        }

        mqtt.lastReceive = Date()

        if (doNotify) {
            service?.let { s ->
                dashboard?.let { d ->
                    createNotification(
                        s,
                        d.name.uppercase(Locale.getDefault()),
                        if (tag.isBlank() || data.second.toString().isBlank())
                            "New value for: ${data.first}"
                        else "$tag: ${data.second.toString()}",
                        silentNotify,
                        d.id.toInt()
                    )
                }
            }
        }

        if (doLog) dashboard?.log?.newEntry("${tag.ifBlank { data.first }}: ${data.second}")
        if (settings.animateUpdate && holder?.itemView?.animation == null) {
            holder?.itemView?.attentate()
        }

        try {
            onReceive(data, jsonResult)
        } catch (e: Exception) {
        }
    }

    data class MqttClientData(
        var isEnabled: Boolean = true
    ) {
        var lastReceive: Date? = null

        val subs = mutableMapOf<String, String>()
        val pubs = mutableMapOf<String, String>()
        val jsonPaths = mutableMapOf<String, String>()
        var payloads: MutableMap<String, String> =
            mutableMapOf("base" to "", "true" to "1", "false" to "0")

        var qos = 0
            set(value) {
                field = minOf(2, maxOf(0, value))
            }

        fun setQos() {

        }

        var payloadIsVar = true
        var payloadIsJson = false
        var doConfirmPub = false
        var doRetain = false
    }
}