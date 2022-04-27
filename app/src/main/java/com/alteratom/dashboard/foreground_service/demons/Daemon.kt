package com.alteratom.dashboard.foreground_service.demons

import com.alteratom.dashboard.IdGenerator
import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.tile.Tile
import com.fasterxml.jackson.annotation.JsonIgnore

abstract class Daemon() : IdGenerator.Indexed {
    override val id = getNewId()
    abstract var isEnabled: Boolean

    @JsonIgnore
    val ds = Dashboards()

    init {
        reportTakenId()
    }

    abstract fun initialize()

    class Dashboards {
        val list = mutableListOf<Dashboard>()

        //todo
        fun getTiles(): MutableList<Tile> {
            return list.fold(mutableListOf(), { tiles, d ->
                tiles.addAll(d.tiles)
                return tiles
            })
        }
    }
}