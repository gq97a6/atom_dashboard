package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class Mqttd(private val context: Context, var d: Dashboard) : Daemon() {

    var isEnabled = true
        get() = d.mqtt.isEnabled && field

    var client = MqttAndroidClientExtended(context, d.mqtt.copy())
    var conHandler = MqttdConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        conHandler.dispatch("init")
    }

    fun notifyOptionsChanged() {
        if (client.isConnected && isEnabled) topicCheck()
        conHandler.dispatch("opt_change")
    }

    fun notifyNewAssignment() {
        client.topics = mutableListOf()
        notifyOptionsChanged()
    }

    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = false) {

        if (!client.isConnected) return

        try {
            val message = MqttMessage()

            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained

            client.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String, qos: Int) {

        if (!client.isConnected) return

        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    client.topics.add(Pair(topic, qos))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
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
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

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

    inner class MqttdConnectionHandler {

        var isDone = MutableLiveData(false)

        private var _isDone = false
            set(value) {
                if (value != field) isDone.postValue(value)
                field = value
            }

        private var isDispatchScheduled = false

        fun dispatch(reason: String) {

            _isDone = client.isConnected == isEnabled && (client.mqtt == d.mqtt || !isEnabled)

            if (!_isDone && !isDispatchScheduled) {
                isDone.postValue(_isDone)
                isDispatchScheduled = true

                if (isEnabled) {
                    if (client.isConnected) client.closeAttempt()
                    else {
                        if (client.mqtt.clientId != d.mqtt.clientId || client.mqtt.URI != d.mqtt.URI)
                            client = MqttAndroidClientExtended(context, d.mqtt.copy())

                        client.mqtt = d.mqtt.copy()
                        client.connectAttempt()
                    }
                } else {
                    client.disconnectAttempt()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    isDispatchScheduled = false
                    dispatch("internal")
                }, 500)
            }
        }
    }

    inner class MqttAndroidClientExtended(
        context: Context,
        var mqtt: Dashboard.MqttData
    ) : MqttAndroidClient(context, mqtt.URI, mqtt.clientId) {

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
            if (isBusy) return
            isBusy = true

            setCallback(object : MqttCallback {
                override fun messageArrived(t: String?, m: MqttMessage) {
                    for (tile in d.tiles) tile.receive(Pair(t ?: "", m))
                    data.postValue(Pair(t ?: "", m))
                }

                override fun connectionLost(cause: Throwable?) {
                    topics = mutableListOf()
                    conHandler.dispatch("con_lost")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            val options = MqttConnectOptions()

            options.isCleanSession = true

            if (d.mqtt.includeCred) {
                options.userName = d.mqtt.username
                options.password = d.mqtt.pass.toCharArray()
            } else {
                options.userName = ""
                options.password = charArrayOf()
            }

            //GENERATE CERT ---------------------------------
            //val caInput = context.getAssets().open("vps.crt")
            //val ca = try {
            //    val cf = CertificateFactory.getInstance("X.509")
            //    cf.generateCertificate(caInput) as X509Certificate
            //} catch (er: java.lang.Exception) {
            //    null
            //} finally {
            //    caInput.close()
            //}
            //GENERATE CERT ---------------------------------

            if (d.mqtt.ssl) {

                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
                trustManagerFactory.init(null as KeyStore?)

                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

                    override fun checkClientTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<X509Certificate>,
                        authType: String
                    ) {
                    }
                })

                val tlsContext = SSLContext.getInstance("TLS")
                tlsContext.init(
                    null,
                    if (d.mqtt.sslTrustAll) trustAllCerts else trustManagerFactory.trustManagers,
                    java.security.SecureRandom()
                )

                options.socketFactory = tlsContext.socketFactory
            }

            try {
                connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        val t = mqtt
                        run {}
                    }
                })
            } catch (e: MqttException) {
            }

            isBusy = false
        }

        fun disconnectAttempt(toClose: Boolean = false) {
            if (isBusy) return
            isBusy = true

            try {
                client.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        unregisterResources()
                        setCallback(null)
                        topics = mutableListOf()
                        if (toClose) close()
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                    }
                })
            } catch (e: MqttException) {
            }

            isBusy = false
        }

        fun closeAttempt() {
            disconnectAttempt(true)
        }
    }
}