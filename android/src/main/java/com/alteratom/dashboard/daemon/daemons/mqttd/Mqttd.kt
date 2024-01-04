package com.alteratom.dashboard.daemon.daemons.mqttd

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.manager.StatusManager
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import java.security.KeyStore
import java.security.cert.Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    private lateinit var client: Mqtt5AsyncClient

    //Current config (assigned after successful connection)
    private var currentConfig = MqttConfig()

    //List of subscribed topics
    private var topics: MutableList<Pair<String, Int>> = mutableListOf()

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    private var isConnected = false

    //Ping send on state change
    override val statePing: MutableLiveData<Nothing?> = MutableLiveData(null)

    //Current state
    override val state: State
        get() = if (manager.isWorking) State.ATTEMPTING
        else try {
            if (!isConnected) State.DISCONNECTED
            else if (currentConfig.ssl && !currentConfig.sslTrustAll) State.CONNECTED_SSL
            else State.CONNECTED
        } catch (e: Exception) {
            State.FAILED
        }

    //Daemon notify response methods -------------------------------------------------------------

    override fun notifyAssigned() {
        super.notifyAssigned()
        manager.dispatch(reason = "assigned")
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (isConnected) manager.dispatch(reason = "discharged")
    }

    override fun notifyConfigChanged() {
        super.notifyConfigChanged()
        if (isConnected && isEnabled && currentConfig == d.mqtt) topicCheck()
        else manager.dispatch(reason = "config")
    }

    // Status manager ------------------------------------------------------------------------------

    private val manager = Manager()

    inner class Manager : StatusManager(context) {
        override fun check(): Boolean {
            return isConnected == isEnabled && (currentConfig == d.mqtt || !isEnabled)
        }

        override fun handle() {
            if (isEnabled) {
                if (isConnected) client.disconnect()
                else {
                    if (currentConfig != d.mqtt) buildClient(d.mqtt.copy())
                    client.connect()
                }
            } else client.disconnect()
        }

        override fun onJobDone() = statePing.postValue(null)
        override fun onJobStart() = statePing.postValue(null)
    }

    // Connection methods -------------------------------------------------------------------------

    fun buildClient(config: MqttConfig) {
        var c = Mqtt5Client.builder()
            .identifier(config.clientId)
            .serverHost(config.address)
            .serverPort(config.port)

        c = c.addConnectedListener {
            isConnected = true
            currentConfig = config
            topicCheck()
            statePing.postValue(null)
        }

        c = c.addDisconnectedListener {
            isConnected = false
            topics = mutableListOf()
            manager.dispatch(reason = "connection")
            statePing.postValue(null)
        }

        //Include credentials if required
        if (config.includeCred) c = c.simpleAuth()
            .username(config.username)
            .password(config.pass.toByteArray())
            .applySimpleAuth()

        //Setup SSL if required
        if (config.ssl) c = c.setupSSL(config)

        //Build client
        client = c.buildAsync()
    }

    private fun Mqtt5ClientBuilder.setupSSL(config: MqttConfig): Mqtt5ClientBuilder {
        val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
        kmfStore.load(null, null)
        kmfStore.setCertificateEntry("cc", config.clientCert)
        config.clientKey?.let {
            kmfStore.setKeyEntry(
                "k",
                it.private,
                config.clientKeyPassword.toCharArray(),
                arrayOf<Certificate?>(config.clientCert)
            )
        }

        var tmfStore: KeyStore? = null
        if (config.caCert != null) {
            tmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
            tmfStore.load(null, null)
            tmfStore.setCertificateEntry("c", config.caCert)
        }

        //Decides which authentication credentials should be sent to the remote host
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(kmfStore, config.clientKeyPassword.toCharArray())

        val alg = TrustManagerFactory.getDefaultAlgorithm()

        //Determines whether remote connection should be trusted or not
        val tmf = if (!config.sslTrustAll) TrustManagerFactory.getInstance(alg)
        else InsecureTrustManagerFactory.getInstance(alg)
        tmf.init(tmfStore)

        return this.sslConfig()
            .keyManagerFactory(kmf)
            .trustManagerFactory(tmf)
            .applySslConfig()
    }

    // MQTT methods ------------------------------------------------------------------------------

    fun publish(topic: String, msg: String, qos: Int = 0, retain: Boolean = false) {
        if (!isConnected) return

        client
            .publishWith()
            .topic(topic)
            .payload(msg.toByteArray())
            .qos(MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE)
            .retain(retain)
            .send()
    }

    private fun subscribe(topic: String, qos: Int) {
        if (!isConnected) return

        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.fromCode(qos) ?: MqttQos.AT_MOST_ONCE)
            .callback {
                for (tile in d.tiles) tile.receive(topic, it)
            }
            .send()
            .thenAccept {
                topics.add(Pair(topic, qos))
            }
    }

    private fun unsubscribe(topic: String, qos: Int) {
        if (!isConnected) return

        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .thenAccept {
                topics.remove(Pair(topic, qos))
            }
    }

    //Manage subscriptions at topic list change
    private fun topicCheck() {
        val list: MutableList<Pair<String, Int>> = mutableListOf()
        for (tile in d.tiles.filter { it.mqtt.isEnabled }) {
            for (t in tile.mqtt.subs) {
                Pair(t.value, tile.mqtt.qos).let {
                    if (!list.contains(it) && t.value.isNotBlank()) {
                        list.add(it)
                    }
                }
            }
        }

        val unsTopics = topics - list.toSet()
        val subTopics = list - topics.toSet()

        try {
            for (t in unsTopics) unsubscribe(t.first, t.second)
            for (t in subTopics) subscribe(t.first, t.second)
        } catch (_: Exception) {
        }
    }

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}