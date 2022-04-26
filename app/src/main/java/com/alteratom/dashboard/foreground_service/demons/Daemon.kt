package com.alteratom.dashboard.foreground_service.demons

import com.alteratom.dashboard.dashboard.Dashboard
import com.alteratom.dashboard.tile.Tile

abstract class Daemon(val ds: Dashboards) {
    class Dashboards {
        val list = listOf<Dashboard>()

        //todo
        fun getTiles(): MutableList<Tile> {
            return list.fold(mutableListOf(), { tiles, d ->
                tiles.addAll(d.tiles)
                return tiles
            })
        }
    }
}