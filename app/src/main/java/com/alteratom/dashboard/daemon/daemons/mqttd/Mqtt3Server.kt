package com.alteratom.dashboard.daemon.daemons.mqttd

import androidx.lifecycle.MutableLiveData
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttClientTransportConfig
import com.hivemq.client.mqtt.MqttClientTransportConfigBuilder
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

//Server class
class Mqtt3Server(private var daemon: Mqttd) {

    var client: Mqtt3AsyncClient = Mqtt3Client.builder().buildAsync()
    var currentConfig = MqttConfig()

    var statePing = MutableLiveData(null)

    val state: State
        get() = if (dispatchJob != null && dispatchJob?.isActive == true) State.ATTEMPTING
        else when (client.state) {
            MqttClientState.CONNECTED -> State.CONNECTED
            MqttClientState.DISCONNECTED -> State.DISCONNECTED
            MqttClientState.CONNECTING -> State.ATTEMPTING
            MqttClientState.DISCONNECTED_RECONNECT -> State.ATTEMPTING
            MqttClientState.CONNECTING_RECONNECT -> State.ATTEMPTING
        }

    private fun connectAttempt() {
        val config = daemon.d.mqtt.copy()

        var transportConfig = MqttClientTransportConfig.builder()
            //.protocols()
            .serverAddress(InetSocketAddress(config.address, config.port))

        if (config.ssl) transportConfig = setupSSL(transportConfig, config)

        client = Mqtt3Client.builder()
            .identifier(config.clientId)
            .transportConfig(transportConfig.build())
            .addDisconnectedListener {
                statePing.postValue(null)
            }
            .addConnectedListener {
                statePing.postValue(null)
                currentConfig = config
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

    private fun disconnectAttempt() = client.disconnect()

    //------------------------------------------------------------------------------------------

    private var dispatchJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(cancel: Boolean = false) {
        if (cancel) {
            dispatchJob?.cancel()
        }

        //Return if already dispatched
        if (dispatchJob != null && dispatchJob?.isActive == true) {
            return
        }

        dispatchJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                //withTimeout(10000) { handleDispatch() }
                handleDispatch()
            } catch (e: Exception) { //Create another coroutine after a delay
                e.printStackTrace()
                delay(1000)
                dispatch(true)
            }
        }
    }

    private suspend fun handleDispatch() {
        statePing.postValue(null)
        while (true) {
            if (daemon.d.mqtt.isEnabled && !daemon.isDischarged) when (client.state) {
                MqttClientState.CONNECTED -> { //check correct config and disconnect if wrong
                    if (currentConfig == daemon.d.mqtt) break
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

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }

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

//val sslConfig = MqttClientSslConfig.builder()
//.mqttConnectTimeout(10000, TimeUnit.MILLISECONDS)
//.socketConnectTimeout(10000, TimeUnit.MILLISECONDS)
//.webSocketConfig(ws)
//.sslConfig(sslConfig)

//client.subscribeWith()
//.topicFilter("gda_switch0s")
//.qos(MqttQos.AT_LEAST_ONCE)
//.callback { x: Mqtt5Publish? -> println(x) }
//.send()

//client.publishWith()
//.topic("gda_switch0s")
//.qos(MqttQos.AT_MOST_ONCE)
//.payload("1".toByteArray())
//.retain(true)
//.send()
//}