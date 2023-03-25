package com.alteratom.dashboard.daemon.daemons

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.ConnectionHandler
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon
import com.alteratom.dashboard.daemon.daemons.mqttdd.MqttConfig
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.*
import javax.net.ssl.*

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    var client: MqttAndroidClientExtended = MqttAndroidClientExtended(context, d.mqtt.copy())

    var handler = MqttdHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val isDone: MutableLiveData<Boolean>
        get() = handler.isDone

    override val status: Status
        get() =
            if (!isEnabled) {
                Status.DISCONNECTED
            } else {
                if (client.isConnected)
                    if (d.mqtt.ssl && !d.mqtt.sslTrustAll) Status.CONNECTED_SSL
                    else Status.CONNECTED
                else if (handler.isDone.value != true) Status.ATTEMPTING
                else Status.FAILED
            }

    override fun notifyAssigned() {
        super.notifyAssigned()
        handler.dispatch("assign")
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (client.isConnected) handler.dispatch("discharge")
        else client.unregisterResources()
    }

    override fun notifyConfigChanged() {
        handler.dispatch("opt_change")
        if (client.isConnected && isEnabled) topicCheck()
    }

    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = false) {

        if (!client.isConnected) return

        try {
            val message = MqttMessage().apply {
                payload = msg.toByteArray()
                this.qos = qos
                isRetained = retained
            }

            client.publish(topic, message, null, object : IMqttActionListener {
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

        if (!client.isConnected) return

        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    client.topics.add(Pair(topic, qos))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun unsubscribe(topic: String, qos: Int) {

        if (!client.isConnected) return

        try {
            client.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    client.topics.remove(Pair(topic, qos))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Manage subscriptions at topic list change
    fun topicCheck() {
        val topics: MutableList<Pair<String, Int>> = mutableListOf()

        for (tile in d.tiles.filter { it.mqtt.isEnabled }) {
            for (t in tile.mqtt.subs) {
                Pair(t.value, tile.mqtt.qos).let {
                    if (!topics.contains(it) && t.value.isNotBlank()) {
                        topics.add(it)
                    }
                }
            }
        }

        val unsubTopics = client.topics - topics.toSet()
        val subTopics = topics - client.topics.toSet()

        for (t in unsubTopics) unsubscribe(t.first, t.second)
        for (t in subTopics) subscribe(t.first, t.second)
    }

    //Class that manages server client class
    inner class MqttdHandler : ConnectionHandler() {
        override fun isDoneCheck(): Boolean =
            client.isConnected == isEnabled && (client.conProp == d.mqtt || !isEnabled)

        override fun handleDispatch() {
            if (isEnabled) {
                if (client.isConnected) client.disconnectAttempt(true)
                else {
                    if (client.conProp.clientId != d.mqtt.clientId || client.conProp.uri != d.mqtt.uri)
                        client = MqttAndroidClientExtended(context, d.mqtt.copy())

                    client.conProp = d.mqtt.copy()
                    client.connectAttempt()
                }
            } else {
                client.disconnectAttempt()
            }
        }
    }

    //Server client class
    inner class MqttAndroidClientExtended(
        context: Context,
        var conProp: MqttConfig
    ) : MqttAndroidClient(context, conProp.uri, conProp.clientId) {

        private var isBusy = false
        var topics: MutableList<Pair<String, Int>> = mutableListOf()

        override fun isConnected(): Boolean {
            return try {
                super.isConnected()
            } catch (e: Exception) {
                false
            }
        }

        override fun removeMessage(token: IMqttDeliveryToken?): Boolean = false
        override fun reconnect() {}
        override fun getInFlightMessageCount(): Int = 0

        fun connectAttempt() {
            //if (isBusy) return
            //isBusy = true

            setCallback(object : MqttCallback {
                override fun messageArrived(t: String?, m: MqttMessage) {
                    for (tile in d.tiles) tile.receive(Pair(t ?: "", m))
                    data.postValue(Pair(t ?: "", m))
                }

                override fun connectionLost(cause: Throwable?) {
                    topics = mutableListOf()
                    handler.dispatch("con_lost")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            val options = MqttConnectOptions()

            options.isCleanSession = true

            if (conProp.includeCred) {
                options.userName = conProp.username
                options.password = conProp.pass.toCharArray()
            } else {
                options.userName = ""
                options.password = charArrayOf()
            }

            if (conProp.ssl) {
                val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
                kmfStore.load(null, null)
                kmfStore.setCertificateEntry("cc", conProp.clientCert)
                conProp.clientKey?.let {
                    kmfStore.setKeyEntry(
                        "k",
                        it.private,
                        conProp.clientKeyPassword.toCharArray(),
                        arrayOf<Certificate?>(conProp.clientCert)
                    )
                }

                val kmf: KeyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
                kmf.init(kmfStore, conProp.clientKeyPassword.toCharArray())

                val tlsContext = SSLContext.getInstance("TLS")
                tlsContext.init(
                    kmf.keyManagers,
                    if (!conProp.sslTrustAll) { //TRUST ONLY IMPORTED
                        val trustManagerFactory = TrustManagerFactory.getInstance(
                            TrustManagerFactory.getDefaultAlgorithm()
                        )

                        trustManagerFactory.init(
                            if (conProp.caCert != null) {
                                KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                                    load(null, null)
                                    setCertificateEntry("c", conProp.caCert)
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

            try {
                connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                        handler.dispatch("con")
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        run {}
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //isBusy = false
        }

        fun disconnectAttempt(close: Boolean = false) {
            //if (isBusy) return
            //isBusy = true

            try {
                client.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        unregisterResources()
                        setCallback(null)
                        topics = mutableListOf()
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                    }
                })

                if (close) close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //isBusy = false
        }
    }

    enum class Status { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }

    class DaemonizedConfig(
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
}
