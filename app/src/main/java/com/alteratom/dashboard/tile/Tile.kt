package com.alteratom.dashboard.tile

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.alteratom.R
import com.alteratom.dashboard.G.settings
import com.alteratom.dashboard.G.theme
import com.alteratom.dashboard.Theme.ColorPallet
import com.alteratom.dashboard.attentate
import com.alteratom.dashboard.createNotification
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.foreground_service.ForegroundService.Companion.service
import com.alteratom.dashboard.foreground_service.demons.Mqttd
import com.alteratom.dashboard.icon.Icons
import com.alteratom.dashboard.performClick
import com.alteratom.dashboard.recycler_view.RecyclerViewAdapter
import com.alteratom.dashboard.recycler_view.RecyclerViewItem
import com.alteratom.dashboard.screenWidth
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonTypeInfo
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

@Suppress("UNUSED")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
abstract class Tile : RecyclerViewItem(), Mqttd.Subject {

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

    @JsonAlias("mqtt")
    override val mqttData = Mqttd.ClientData()

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

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = pallet
        )
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        if (mqttData.doNotify) {
            service?.let { s ->
                dashboard?.let { d ->
                    createNotification(
                        s,
                        d.name.uppercase(Locale.getDefault()),
                        if (tag.isBlank() || data.second.toString().isBlank())
                            "New value for: ${data.first}"
                        else "$tag: ${data.second.toString()}",
                        mqttData.silentNotify,
                        d.id.toInt()
                    )
                }
            }
        }

        if (mqttData.doLog) dashboard?.log?.newEntry("${tag.ifBlank { data.first }}: ${data.second}")
        if (settings.animateUpdate && holder?.itemView?.animation == null) {
            holder?.itemView?.attentate()
        }
    }
}