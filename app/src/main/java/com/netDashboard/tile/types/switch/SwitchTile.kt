package com.netDashboard.tile.types.switch

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class SwitchTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_switch

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}