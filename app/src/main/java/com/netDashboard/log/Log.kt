package com.netDashboard.log


class Log {
    val list = mutableListOf(LogEntry(), LogEntry(), LogEntry())

    fun newEntry(text: String) {
        list.add(LogEntry(text))
        if (list.size > 30) list.removeFirst()
    }

    fun flush() = list.clear()
}