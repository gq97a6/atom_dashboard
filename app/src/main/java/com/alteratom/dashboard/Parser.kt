package com.alteratom.dashboard

import com.alteratom.dashboard.G.mapper

object Parser {
    fun String.byJSONPath(path: String): String? =
        try {
            mapper.readTree(this).at(path).asText()
        } catch (e: Exception) {
            null
        }
}