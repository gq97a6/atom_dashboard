package com.netDashboard.tile.types.time

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class TimeTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_time

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}