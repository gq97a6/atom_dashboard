package com.alteratom.dashboard.tile

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.attentate
import com.alteratom.dashboard.createNotification
import com.alteratom.dashboard.daemon.Daemonized
import com.alteratom.dashboard.daemon.daemons.mqttd.MqttDaemonizedConfig
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.`object`.G.settings
import com.alteratom.dashboard.`object`.G.theme
import com.alteratom.dashboard.performClick
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.screenWidth
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.Date
import java.util.Random

@Suppress("UNUSED")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
//@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
//@JsonSubTypes(
//    JsonSubTypes.Type(value = ButtonTile::class, name = "CommentNote"),
//    JsonSubTypes.Type(value = SwitchTile::class, name = "PhotoNote")
//)
abstract class Tile : RecyclerViewItem(), Daemonized {

    @JsonIgnore
    override var dashboard: Dashboard? = null

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

    @JsonAlias("mqttData")
    override val mqtt: MqttDaemonizedConfig = MqttDaemonizedConfig()

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

        updateTimer()
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = pallet
        )
    }

    override fun onReceive(
        topic: String,
        msg: MqttMessage,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(topic, msg, jsonResult)

        if (this.mqtt.doNotify && !msg.isRetained) {
            dashboard?.daemon?.context?.let { context ->
                createNotification(
                    context,
                    mqtt.notifyTitle.replace("@v", msg.toString()),
                    mqtt.notifyPayload.replace("@v", msg.toString()),
                    this.mqtt.silentNotify,
                    if (settings.notifyStack) 999 else Random().nextInt()
                )
            }
        }

        if (this.mqtt.doLog) dashboard?.log?.newEntry("${tag.ifBlank { topic }}: $msg")
        if (settings.animateUpdate && holder?.itemView?.animation == null) {
            holder?.itemView?.attentate()
        }
    }

    fun updateTimer() {
        val time = Date().time - (mqtt.lastReceive?.time ?: return)

        (time / 1000).let { s ->
            if (s < 60) if (s == 1L) "$s second ago" else "$s seconds ago"
            else (time / 60000).let { m ->
                if (m < 60) if (m == 1L) "$m minute ago" else "$m minutes ago"
                else (time / 3600000).let { h ->
                    if (h < 24) if (h == 1L) "$h hour ago" else "$h hours ago"
                    else (time / 86400000).let { d ->
                        if (d < 365) if (d == 1L) "$d day ago" else "$d days ago"
                        else (time / 31536000000).let { y ->
                            if (y == 1L) "$y year ago" else "$y years ago"
                        }
                    }
                }
            }
        }.apply {
            holder?.itemView?.findViewById<TextView>(R.id.t_status)?.text = this
        }
    }
}