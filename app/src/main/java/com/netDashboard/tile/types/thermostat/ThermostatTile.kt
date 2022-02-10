package com.netDashboard.tile.types.thermostat

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netDashboard.R
import com.netDashboard.tile.Tile

class ThermostatTile : Tile() {

    @JsonIgnore
    override val layout = R.layout.tile_thermostat

    override val mqttData = MqttData("")

    @JsonIgnore
    override var typeTag = "button"
}