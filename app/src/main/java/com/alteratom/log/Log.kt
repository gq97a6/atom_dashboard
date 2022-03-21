package com.alteratom.log


class Log {
    val list: MutableList<LogEntry> = mutableListOf()

    fun newEntry(text: String) {
        list.add(0, LogEntry(text))
        if (list.size > 30) list.removeLast()
    }

    fun flush() = list.clear()
}