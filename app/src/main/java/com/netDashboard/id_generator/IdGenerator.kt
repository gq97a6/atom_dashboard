package com.netDashboard.id_generator

import java.util.*

object IdGenerator {

    private val takenIds = mutableListOf<Long>()

    fun getId(): Long {
        while (true) {
            kotlin.math.abs(Random().nextLong()).let {
                if (!takenIds.contains(it)) {
                    takenIds.add(it)
                    return it
                }
            }
        }
    }

    fun reportTakenId(id: Long) = takenIds.add(id)
}


