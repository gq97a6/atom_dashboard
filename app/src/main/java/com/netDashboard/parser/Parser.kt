package com.netDashboard.parser

import com.google.gson.JsonArray
import com.netDashboard.globals.G.gson
import com.netDashboard.settings.Settings

object Parser {
    fun String.parse() {
        try {
            gson.fromJson(this, JsonArray::class.java)
        } catch (e: Exception) {
            Settings()
        }
    }
    //value, other value, day, month, hour, year
}