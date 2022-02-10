package com.netDashboard.tile.types.terminal

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class TerminalTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_terminal

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}