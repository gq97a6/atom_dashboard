package com.netDashboard.tile.types.thermostat

import android.view.View
import android.widget.TextView
import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.recycler_view.RecyclerViewAdapter
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class ThermostatTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_thermostat

    @JsonIgnore
    override var typeTag = "thermostat"

    override var iconRes = R.drawable.il_weather_temperature_half

    var humiditySetpoint = false
    var showPayload = false
    val modes = mutableListOf("Auto" to "0", "Heat" to "1", "Cool" to "2", "Off" to "3")
    var humidityRange = mutableListOf(0f, 100f, 5f)
    var temperatureRange = mutableListOf(0f, 30f, .5f)

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (tag.isBlank()) holder.itemView.findViewById<TextView>(R.id.t_tag)?.visibility =
            View.GONE
    }

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        val value = jsonResult["base"] ?: data.second.toString()
    }
}