package com.netDashboard.tile.types.pick

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class SelectTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_select

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}