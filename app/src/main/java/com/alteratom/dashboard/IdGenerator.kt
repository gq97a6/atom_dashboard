package com.alteratom.dashboard

import java.util.*

object IdGenerator {

    private val takenIds = mutableListOf<Long>()

    interface Indexed {
        val id: Long

        fun reportTakenId() = takenIds.add(id)

        fun getNewId(): Long {
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
}
