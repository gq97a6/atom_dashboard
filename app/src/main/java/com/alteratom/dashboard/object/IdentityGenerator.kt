package com.alteratom.dashboard.`object`

import java.util.Random

object IdentityGenerator {

    private val takenIds = mutableListOf<Long>()

    fun reportTakenId(id: Long) = takenIds.add(id)

    fun obtainNewId(): Long {
        while (true) {
            kotlin.math.abs(Random().nextLong()).let {
                if (!takenIds.contains(it)) {
                    takenIds.add(it)
                    return it
                }
            }
        }
    }
}
