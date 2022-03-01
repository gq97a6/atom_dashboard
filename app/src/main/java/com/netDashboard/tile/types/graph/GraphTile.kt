package com.netDashboard.tile.types.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile
import org.eclipse.paho.client.mqttv3.MqttMessage

class GraphTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_graph

    @JsonIgnore
    override var typeTag = "button"

    override fun onReceive(
        data: Pair<String?, MqttMessage?>,
        jsonResult: MutableMap<String, String>
    ) {
        val value = jsonResult["base"] ?: data.second.toString()
    }
}