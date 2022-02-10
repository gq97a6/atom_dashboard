package com.netDashboard.tile.types.color

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class ColorTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_color

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}