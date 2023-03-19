package com.alteratom.dashboard.objects

import java.util.*

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

    interface Indexed {
        val id: Long

        fun reportTakenId() = reportTakenId(id)

        fun obtainNewId(): Long = IdentityGenerator.obtainNewId()
    }
}
