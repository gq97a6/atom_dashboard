package com.alteratom.dashboard.daemon.daemons.mqttd

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.daemon.Daemon
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttClientTransportConfig
import com.hivemq.client.mqtt.MqttClientTransportConfigBuilder
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.security.KeyStore
import java.security.cert.Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

//Empty test class
class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val statePing: MutableLiveData<Nothing?> = MutableLiveData(null)

    override val state: State
        get() = State.DISCONNECTED

    fun publish(topic: String, msg: String, qos: Int = 0, retain: Boolean = false) {
    }

    private fun subscribe(topic: String, qos: Int) {
    }

    private fun unsubscribe(topic: String, qos: Int) {
    }

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}