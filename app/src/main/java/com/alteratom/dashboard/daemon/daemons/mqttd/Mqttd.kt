package com.alteratom.dashboard.daemon.daemons.mqttd

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon
import kotlinx.coroutines.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.*

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    var client = MqttAndroidClient(context, d.mqtt.uri, d.mqtt.clientId)

    private var currentConfig = MqttConfig()
    private var topics: MutableList<Pair<String, Int>> = mutableListOf()

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    private val isConnected: Boolean
        get() = try {
            client.isConnected
        } catch (e: Exception) {
            false
        }

    override val statePing: MutableLiveData<Nothing?> = MutableLiveData(null)
    override val state: State
        get() = if (dispatchJob != null && dispatchJob?.isActive == true) State.ATTEMPTING
        else try {
            if (!client.isConnected) State.DISCONNECTED
            else if (d.mqtt.ssl && !d.mqtt.sslTrustAll) State.CONNECTED_SSL
            else State.CONNECTED
        } catch (e: Exception) {
            State.FAILED
        }

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
    private fun dispatch(cancel: Boolean = false) {
        //TODO: LOG
        d.log.newEntry("dispatch")

        if (cancel) dispatchJob?.cancel()

        //TODO: LOG
        d.log.newEntry("ds1")

        //Return if already dispatched
        if (dispatchJob != null && dispatchJob?.isActive == true) {
            //TODO: LOG
            d.log.newEntry("already dispatch")

            return
        }

        //TODO: LOG
        d.log.newEntry("ds2")

        (context as LifecycleOwner).apply {

            //TODO: LOG
            d.log.newEntry("ds3")

            dispatchJob = lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        //TODO: LOG
                        d.log.newEntry("ds4")

                        handleDispatch()
                    } catch (e: Exception) { //Create another coroutine after a delay
                        //TODO: LOG
                        d.log.newEntry("dispatch exception")

                        delay(1000)
                        dispatch(true)
                    }
        }
    }
}

//Try to stabilize the connection
private suspend fun handleDispatch() {
    statePing.postValue(null)

    //TODO: LOG
    d.log.newEntry("handle dispatch")

    while (true) {
        if (client.isConnected == isEnabled && (currentConfig == d.mqtt || !isEnabled)) {
            //TODO: LOG
            d.log.newEntry(
                "done{ " +
                        "c${if (client.isConnected) "1" else "0"} | " +
                        "e${if (isEnabled) "1" else "0"} | " +
                        "f${if (currentConfig == d.mqtt) "1" else "0"} }"
            )
            break
        }

        if (isEnabled) {
            if (client.isConnected) disconnectAttempt(true)
            else {
                if (client.clientId != d.mqtt.clientId || client.serverURI != d.mqtt.uri) {
                    //TODO: LOG
                    d.log.newEntry("new client")

                    client = MqttAndroidClient(context, d.mqtt.uri, d.mqtt.clientId)
                }

                connectAttempt(d.mqtt.copy())
            }
        } else disconnectAttempt()

        delay(1000)
    }

    statePing.postValue(null)
}

// Connection methods -------------------------------------------------------------------------

private fun disconnectAttempt(close: Boolean = false) {
    //TODO: LOG
    d.log.newEntry("disconn{$close}")

    try {
        client.disconnect(null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                client.unregisterResources()
                client.setCallback(null)
                topics = mutableListOf()

                //TODO: LOG
                d.log.newEntry("disconn succ")
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                //TODO: LOG
                d.log.newEntry("disconn failure")
            }
        })

        if (close) client.close()
    } catch (_: Exception) {
        //TODO: LOG
        d.log.newEntry("disconn exception")
    }
}

private fun connectAttempt(config: MqttConfig) {
    //TODO: LOG
    d.log.newEntry("con")

    client.setCallback(object : MqttCallback {
        override fun messageArrived(topic: String?, msg: MqttMessage) {
            for (tile in d.tiles) tile.receive(topic ?: "", msg)
        }

        override fun connectionLost(cause: Throwable?) {
            topics = mutableListOf()
            dispatch()
        }

        override fun deliveryComplete(token: IMqttDeliveryToken?) {
        }
    })

    val options = MqttConnectOptions()

    options.isCleanSession = true

    if (config.includeCred) {
        options.userName = config.username
        options.password = config.pass.toCharArray()
    } else {
        options.userName = ""
        options.password = charArrayOf()
    }

    options.keepAliveInterval = 0

    if (config.ssl) setupSSL(config, options)

    try {
        client.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                topicCheck()
                currentConfig = config

                //TODO: LOG
                d.log.newEntry("con succ")
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                //TODO: LOG
                d.log.newEntry("con failure")
            }
        })
    } catch (_: Exception) {
        //TODO: LOG
        d.log.newEntry("con exception")
    }
}

private fun setupSSL(config: MqttConfig, options: MqttConnectOptions) {
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

    val kmf: KeyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(kmfStore, config.clientKeyPassword.toCharArray())

    val tlsContext = SSLContext.getInstance("TLS")
    tlsContext.init(
        kmf.keyManagers,
        if (!config.sslTrustAll) { //TRUST ONLY IMPORTED
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )

            trustManagerFactory.init(
                if (config.caCert != null) {
                    KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                        load(null, null)
                        setCertificateEntry("c", config.caCert)
                    }
                } else null
            )

            trustManagerFactory.trustManagers
        } else { //TRUST ALL CERTS
            arrayOf<TrustManager>(
                @SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> =
                        emptyArray()

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }
                }
            )
        },
        java.security.SecureRandom()
    )

    options.socketFactory = tlsContext.socketFactory
}

// MQTT methods ------------------------------------------------------------------------------

fun publish(topic: String, msg: String, qos: Int = 0, retain: Boolean = false) {
    if (!client.isConnected) return

    try {
        client.publish(
            topic,
            msg.toByteArray(),
            qos,
            retain,
            null,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun subscribe(topic: String, qos: Int) {
    if (!isConnected) return

    try {
        client.subscribe(topic, qos, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                topics.add(Pair(topic, qos))
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            }
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun unsubscribe(topic: String, qos: Int) {
    if (!isConnected) return

    try {
        client.unsubscribe(topic, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                topics.remove(Pair(topic, qos))
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
            }
        })
    } catch (e: Exception) {
        e.printStackTrace()
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

    val unsubTopics = topics - list.toSet()
    val subTopics = list - topics.toSet()

    try {
        for (t in unsubTopics) unsubscribe(t.first, t.second)
        for (t in subTopics) subscribe(t.first, t.second)
    } catch (_: Exception) {
    }
}

enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}