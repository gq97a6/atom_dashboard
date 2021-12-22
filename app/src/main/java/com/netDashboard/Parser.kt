package com.netDashboard

import com.netDashboard.globals.G.mapper

object Parser {
    fun String.byJSONPath(path: String): String? =
        try {
            mapper.readTree(this).at(path).asText()
        } catch (e: Exception) {
            null
        }

    //value of tile, value of other tiles, second of day, second, minute, hour, day, month, year
    //to json
}