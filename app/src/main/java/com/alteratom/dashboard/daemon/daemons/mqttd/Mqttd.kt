package com.alteratom.dashboard.daemon.daemons.mqttd

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.ForegroundService
import com.alteratom.dashboard.Logger
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

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    var client: Mqtt3AsyncClient = Mqtt3Client.builder().buildAsync()
    private var currentConfig = MqttConfig()

    //var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))
    private var topics: MutableList<Pair<String, Int>> = mutableListOf()

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val statePing: MutableLiveData<Nothing?> = MutableLiveData(null)

    private val isConnected: Boolean
        get() = try {
            client.state.isConnected
        } catch (e: Exception) {
            Logger.log(e.stackTraceToString())
            false
        }

    override val state: State
        get() = if (dispatchJob != null && dispatchJob?.isActive == true) State.ATTEMPTING
        else when (client.state) {
            MqttClientState.CONNECTED -> State.CONNECTED
            MqttClientState.DISCONNECTED -> State.DISCONNECTED
            MqttClientState.CONNECTING -> State.ATTEMPTING
            MqttClientState.DISCONNECTED_RECONNECT -> State.ATTEMPTING
            MqttClientState.CONNECTING_RECONNECT -> State.ATTEMPTING
        }

    //if (!isEnabled) Status.DISCONNECTED
    //else {
    //    if (server.isConnected)
    //        if (d.mqtt.ssl && !d.mqtt.sslTrustAll) Status.CONNECTED_SSL
    //        else Status.CONNECTED
    //    else if (manager.isDone.value != true) Status.ATTEMPTING
    //    else Status.FAILED
    //}

    //Daemon notify response methods -------------------------------------------------------------

    override fun notifyAssigned() {
        super.notifyAssigned()
        dispatch()
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (isConnected) dispatch()
    }

    override fun notifyConfigChanged() {
        super.notifyConfigChanged()
        if (isConnected && isEnabled && currentConfig == d.mqtt) topicCheck()
        else dispatch()
    }

    // Status manger ------------------------------------------------------------------------------

    //Current job
    private var dispatchJob: Job? = null

    //Start the manager if not already running
    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(cancel: Boolean = false) {
        Logger.log("mqttd dispatch")
        if (cancel) {
            dispatchJob?.cancel()
        }

        //Return if already dispatched
        if (dispatchJob != null && dispatchJob?.isActive == true) {
            Logger.log("mqttd dispatched already")
            return
        }

        (context as ForegroundService).apply {
            dispatchJob = lifecycleScope.launch(Dispatchers.IO) {
                try {
                    //withTimeout(10000) { handleDispatch() }
                    handleDispatch()
                } catch (e: Exception) { //Create another coroutine after a delay
                    Logger.log(e.stackTraceToString())
                    delay(1000)
                    dispatch(true)
                }
            }
        }
    }

    //Try to stabilize the connection
    private suspend fun handleDispatch() {
        Logger.log("mqttd handle")
        statePing.postValue(null)
        while (true) {
            if (d.mqtt.isEnabled && !isDischarged) when (client.state) {
                MqttClientState.CONNECTED -> { //check correct config and disconnect if wrong
                    if (currentConfig == d.mqtt) break
                    else disconnectAttempt()
                }
                MqttClientState.DISCONNECTED -> { //set config and connect
                    connectAttempt()
                }
                MqttClientState.CONNECTING -> {} //recheck
                MqttClientState.DISCONNECTED_RECONNECT -> {} //recheck
                MqttClientState.CONNECTING_RECONNECT -> {} //recheck
            } else when (client.state) {
                MqttClientState.CONNECTED -> disconnectAttempt()
                MqttClientState.DISCONNECTED -> break
                MqttClientState.CONNECTING -> {} //recheck
                MqttClientState.DISCONNECTED_RECONNECT -> {} //recheck
                MqttClientState.CONNECTING_RECONNECT -> {} //recheck
            }

            delay(1000)
        }
        statePing.postValue(null)
    }

    // Connection methods -------------------------------------------------------------------------

    private fun disconnectAttempt() = client.disconnect()

    private fun connectAttempt() {
        val config = d.mqtt.copy()

        var transportConfig = MqttClientTransportConfig.builder()
            //.protocols()
            .serverAddress(InetSocketAddress(config.address, config.port))

        if (config.ssl) transportConfig = setupSSL(transportConfig, config)

        client = Mqtt3Client.builder()
            .identifier(config.clientId)
            .transportConfig(transportConfig.build())
            .addDisconnectedListener {
                Logger.log("mqttd disconnected")
                statePing.postValue(null)
                topics = mutableListOf()
                dispatch()
            }
            .addConnectedListener {
                Logger.log("mqttd connected")
                statePing.postValue(null)
                currentConfig = config
                topicCheck()
            }
            .buildAsync()

        client.state
        client.connectWith()
            .simpleAuth()
            .username(config.username)
            .password(config.pass.toByteArray())
            .applySimpleAuth()
            .keepAlive(30)
            .cleanSession(true)
            .send()
    }

    private fun setupSSL(
        transportConfig: MqttClientTransportConfigBuilder,
        config: MqttConfig
    ): MqttClientTransportConfigBuilder {

        val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
        kmfStore.load(null, null)
        kmfStore.setCertificateEntry("", config.clientCert)
        config.clientKey?.let {
            kmfStore.setKeyEntry(
                "k",
                it.private,
                config.clientKeyPassword.toCharArray(),
                arrayOf<Certificate?>(config.clientCert)
            )
        }

        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(kmfStore, config.clientKeyPassword.toCharArray())

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(if (config.caCert != null) {
            KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                load(null, null)
                setCertificateEntry("", config.caCert)
            }
        } else null)

        //val hv = HostnameVerifier { hostname, session ->
        //    true
        //}

        return transportConfig
            .sslConfig()
            //.handshakeTimeout(1, TimeUnit.SECONDS)
            //.hostnameVerifier(hv)
            .keyManagerFactory(kmf)
            .trustManagerFactory(tmf)
            .applySslConfig()
    }

    // MQTT methods ------------------------------------------------------------------------------

    fun publish(topic: String, msg: String, qos: Int = 0, retain: Boolean = false) {
        if (!isConnected) return
        client.publishWith()
            .topic(topic)
            .qos(MqttQos.fromCode(qos) ?: MqttQos.AT_LEAST_ONCE)
            .payload(msg.toByteArray())
            .retain(retain)
            .send()
    }

    private fun subscribe(topic: String, qos: Int) {
        if (!isConnected) return
        client.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.fromCode(qos) ?: MqttQos.AT_LEAST_ONCE)
            .callback {
                for (tile in d.tiles) tile.receive(it)
                //data.postValue(Pair(t ?: "", m))
            }
            .send()
            .whenCompleteAsync { _, u -> if (u != null) topics.add(Pair(topic, qos)) }
    }

    private fun unsubscribe(topic: String, qos: Int) {
        if (!isConnected) return
        client.unsubscribeWith()
            .topicFilter(topic)
            .send()
            .whenCompleteAsync { _, u -> if (u != null) topics.remove(Pair(topic, qos)) }
    }

    //Manage subscriptions at topic list change
    private fun topicCheck() {
        Logger.log("topic check")
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

        val unsubTopics = topics - list.toSet()
        val subTopics = list - topics.toSet()

        for (t in unsubTopics) unsubscribe(t.first, t.second)
        for (t in subTopics) subscribe(t.first, t.second)
    }

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}

//private fun setupSSL(options: MqttConnectOptions) {
//    val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
//    kmfStore.load(null, null)
//    kmfStore.setCertificateEntry("cc", conProp.clientCert)
//    conProp.clientKey?.let {
//        kmfStore.setKeyEntry(
//            "k",
//            it.private,
//            conProp.clientKeyPassword.toCharArray(),
//            arrayOf<Certificate?>(conProp.clientCert)
//        )
//    }
//
//    val kmf: KeyManagerFactory =
//        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
//    kmf.init(kmfStore, conProp.clientKeyPassword.toCharArray())
//
//    val trustManager = if (!conProp.sslTrustAll) { //TRUST ONLY IMPORTED
//        val trustManagerFactory = TrustManagerFactory.getInstance(
//            TrustManagerFactory.getDefaultAlgorithm()
//        )
//
//        trustManagerFactory.init(
//            if (conProp.caCert != null) {
//                KeyStore.getInstance(KeyStore.getDefaultType()).apply {
//                    load(null, null)
//                    setCertificateEntry("c", conProp.caCert)
//                }
//            } else null
//        )
//
//        trustManagerFactory.trustManagers
//    } else { //TRUST ALL CERTS
//        arrayOf<TrustManager>(
//            @SuppressLint("CustomX509TrustManager")
//            object : X509TrustManager {
//                override fun getAcceptedIssuers(): Array<X509Certificate> =
//                    emptyArray()
//
//                @SuppressLint("TrustAllX509TrustManager")
//                override fun checkClientTrusted(
//                    chain: Array<X509Certificate>,
//                    authType: String
//                ) {
//                }
//
//                @SuppressLint("TrustAllX509TrustManager")
//                override fun checkServerTrusted(
//                    chain: Array<X509Certificate>,
//                    authType: String
//                ) {
//                }
//            }
//        )
//    }
//
//    val tlsContext = SSLContext.getInstance("TLS")
//    tlsContext.init(kmf.keyManagers, trustManager, java.security.SecureRandom())
//
//    options.socketFactory = tlsContext.socketFactory
//}