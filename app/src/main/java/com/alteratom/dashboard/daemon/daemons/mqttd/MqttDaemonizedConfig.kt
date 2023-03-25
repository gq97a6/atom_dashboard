package com.alteratom.dashboard.daemon.daemons.mqttd

import java.util.*

class MqttDaemonizedConfig(
    var isEnabled: Boolean = true,
    var lastReceive: Date? = null,
    val subs: MutableMap<String, String> = mutableMapOf(),
    val pubs: MutableMap<String, String> = mutableMapOf(),
    val jsonPaths: MutableMap<String, String> = mutableMapOf(),
    var payloads: MutableMap<String, String> = mutableMapOf(
        "base" to "",
        "true" to "1",
        "false" to "0"
    ),
    private var _qos: Int = 0,
    var payloadIsVar: Boolean = true,
    var payloadIsJson: Boolean = false,
    var doConfirmPub: Boolean = false,
    var doRetain: Boolean = false,
    var doLog: Boolean = false,
    var doNotify: Boolean = false,
    var silentNotify: Boolean = false
) {
    var qos
        set(value) {
            _qos = minOf(2, maxOf(0, value))
        }
        get() = _qos
}