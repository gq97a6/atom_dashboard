package com.netDashboard.tile.types.switch

import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.Theme
import com.netDashboard.globals.G.theme
import com.netDashboard.icon.Icons
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class SwitchTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_switch

    @JsonIgnore
    override var typeTag = "switch"

    override var iconKey = "il_interface_toggle_on"

    var state: Boolean? = false

    var iconKeyTrue = "il_interface_toggle_on"
    val iconResTrue: Int
        get() = Icons.icons[iconKeyTrue]?.res ?: R.drawable.il_interface_toggle_on

    var iconKeyFalse = "il_interface_toggle_off"
    val iconResFalse: Int
        get() = Icons.icons[iconKeyFalse]?.res ?: R.drawable.il_interface_toggle_off

    var hsvTrue = floatArrayOf(179f, 1f, 1f)
    val colorPalletTrue: Theme.ColorPallet
        get() = theme.a.getColorPallet(hsvTrue, true)

    var hsvFalse = floatArrayOf(0f, 0f, 0f)
    val colorPalletFalse: Theme.ColorPallet
        get() = theme.a.getColorPallet(hsvFalse, true)

    private val colorPalletState
        get() = when (state) {
            true -> colorPalletTrue
            false -> colorPalletFalse
            null -> colorPallet
        }

    private val iconResState
        get() = when (state) {
            true -> {
                iconResTrue
            }
            false -> {
                iconResFalse
            }
            null -> {
                iconRes
            }
        }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
        holder.itemView.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconResState)
    }

    override fun onClick(v: View, e: MotionEvent) {
        super.onClick(v, e)

        send(mqttData.payloads[if (state == false) "true" else "false"] ?: "", mqttData.qos)
    }

    override fun onSetTheme(holder: RecyclerViewAdapter.ViewHolder) {
        theme.apply(
            holder.itemView as ViewGroup,
            anim = false,
            colorPallet = colorPalletState
        )
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        super.onReceive(data, jsonResult)

        state = when (data.second.toString()) {
            mqttData.payloads["true"] -> {
                true
            }
            mqttData.payloads["false"] -> {
                false
            }
            else -> null
        }

        holder?.itemView?.findViewById<View>(R.id.t_icon)?.setBackgroundResource(iconResState)

        holder?.itemView?.let {
            theme.apply(
                it as ViewGroup,
                anim = false,
                colorPallet = colorPalletState
            )
        }
    }
}