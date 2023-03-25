package com.alteratom.dashboard.daemon

import androidx.annotation.IntRange
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.daemons.Mqttd
import com.alteratom.dashboard.objects.DialogBuilder.buildConfirm
import com.alteratom.dashboard.objects.Storage
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.util.*

//For targets of daemons
interface Daemonized {
    var dashboard: Dashboard?

    //Configuration for each daemon
    val mqtt: Mqttd.DaemonizedConfig

    fun onSend(
        topic: String?,
        msg: String,
        @IntRange(from = 0, to = 2) qos: Int,
        retained: Boolean = false
    ) {
    }

    fun onReceive(data: Pair<String?, MqttMessage?>, jsonResult: MutableMap<String, String>) {
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

    fun receive(data: Pair<String?, MqttMessage?>) {
        if (!this.mqtt.isEnabled) return
        if (!this.mqtt.subs.containsValue(data.first)) return

        //Build map of jsonPath key and value. Null on absence or fail.
        val jsonResult = mutableMapOf<String, String>()
        if (this.mqtt.payloadIsJson) {
            for (p in this.mqtt.jsonPaths) {
                data.second.toString().let { it ->
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
            onReceive(data, jsonResult)
        } catch (e: Exception) {
        }
    }

    class Data {

    }
}