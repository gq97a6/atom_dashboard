package com.netDashboard.tile.types.graph

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class GraphTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_graph

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}