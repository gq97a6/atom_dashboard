package com.alteratom.dashboard.daemon

import androidx.annotation.IntRange
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.daemons.mqttd.MqttDaemonizedConfig
import com.alteratom.dashboard.daemon.daemons.mqttd.Mqttd
import com.alteratom.dashboard.objects.DialogBuilder.buildConfirm
import com.alteratom.dashboard.objects.Logger
import com.alteratom.dashboard.objects.Storage
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

//For targets of daemons
interface Daemonized {
    var dashboard: Dashboard?

    //Configuration for each daemon
    val mqtt: MqttDaemonizedConfig

    fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ) {
    }

    fun onReceive(data: Pair<String, String>, jsonResult: MutableMap<String, String>) {
    }

    fun send(
        msg: String,
        topic: String? = mqtt.pubs["base"],
        @IntRange(from = 0, to = 2) qos: Int = mqtt.qos,
        retain: Boolean = mqtt.doRetain,
        raw: Boolean = false
    ) {
        if (dashboard?.daemon !is Mqttd) return
        if (topic.isNullOrEmpty()) return

        val commit = {
            (dashboard?.daemon as Mqttd).publish(topic, msg, qos, retain)
            onSend(topic, msg, qos, retain)
        }

        if (!mqtt.doConfirmPub || raw) commit()
        else dashboard?.tiles?.first()?.adapter?.context?.let {
            with(it) { buildConfirm("Confirm publishing", "PUBLISH") { commit() } }
        }
    }

    //data: Pair<String?, MqttMessage?>
    fun receive(topic: String, msg: MqttMessage) {
        if (!this.mqtt.subs.containsValue(topic)) return

        //Build map of jsonPath key and value. Null on absence or fail.
        val jsonResult = mutableMapOf<String, String>()
        if (this.mqtt.payloadIsJson) {
            for (p in this.mqtt.jsonPaths) {
                msg.toString().let { it ->
                    try {
                        Storage.mapper.readTree(it).at(p.value).asText()
                    } catch (e: Exception) {
                        null
                    }?.let {
                        jsonResult[p.key] = it
                    }
                }
            }
        }

        this.mqtt.lastReceive = Date()

        try {
            onReceive(
                Pair(
                    topic,
                    msg.toString()
                ), jsonResult
            )
        } catch (e: Exception) {
            Logger.log(e.stackTraceToString())
        }
    }
}