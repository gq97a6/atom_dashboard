package com.netDashboard.tile.types.lights

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class LightsTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_lights

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}