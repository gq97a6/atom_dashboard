package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.dashboard.Dashboard
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.security.KeyStore
import java.security.Security
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    var client: MqttAndroidClientExtended = MqttAndroidClientExtended(context, d.mqtt.copy())

    var conHandler = ConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val isDone: MutableLiveData<Boolean>
        get() = conHandler.isDone

    override val status: MqttdStatus
        get() =
            if (!isEnabled) {
                MqttdStatus.DISCONNECTED
            } else {
                if (client.isConnected)
                    if (d.mqtt.ssl && !d.mqtt.sslTrustAll) MqttdStatus.CONNECTED_SSL
                    else MqttdStatus.CONNECTED
                else if (conHandler.isDone.value != true) MqttdStatus.ATTEMPTING
                else MqttdStatus.FAILED
            }

    override fun notifyAssigned() {
        super.notifyAssigned()
        conHandler.dispatch("assign")
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (client.isConnected) conHandler.dispatch("discharge")
        else client.unregisterResources()
    }

    override fun notifyOptionsChanged() {
        conHandler.dispatch("opt_change")
        if (client.isConnected && isEnabled) topicCheck()
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

    inner class ConnectionHandler : DaemonConnectionHandler() {

        override fun isDone(): Boolean =
            client.isConnected == isEnabled && (client.conProp == d.mqtt || !isEnabled)

        override fun handleDispatch() {
            if (isEnabled) {
                if (client.isConnected) client.closeAttempt()
                else {
                    if (client.conProp.clientId != d.mqtt.clientId || client.conProp.URI != d.mqtt.URI)
                        client = MqttAndroidClientExtended(context, d.mqtt.copy())

                    client.conProp = d.mqtt.copy()
                    client.connectAttempt()
                }
            } else {
                client.disconnectAttempt()
            }
        }
    }

    inner class MqttAndroidClientExtended(
        context: Context,
        var conProp: Dashboard.MqttData
    ) : MqttAndroidClient(context, conProp.URI, conProp.clientId) {

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

            if (conProp.includeCred) {
                options.userName = conProp.username
                options.password = conProp.pass.toCharArray()
            } else {
                options.userName = ""
                options.password = charArrayOf()
            }

            if (conProp.ssl) {

                val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
                val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)

                trustManagerFactory.init(
                    if (conProp.sslCert != null) {
                        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
                        keyStore.load(null, null)
                        keyStore.setCertificateEntry("ca", conProp.sslCert)
                        keyStore
                    } else null
                )

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
                    if (conProp.sslTrustAll) trustAllCerts else trustManagerFactory.trustManagers,
                    java.security.SecureRandom()
                )

                options.socketFactory = tlsContext.socketFactory
            }

            try {
                connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                        conHandler.dispatch("con")
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

    enum class MqttdStatus { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}

fun getSocketFactory(
    caCrtFile: InputStream?, crtFile: InputStream?, keyFile: InputStream?,
    password: String
): SSLSocketFactory? {
    Security.addProvider(BouncyCastleProvider())

    // --------------------------------------------------------------------------------------------

    var caCert: X509Certificate? = null
    var bis = BufferedInputStream(caCrtFile)
    val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
    while (bis.available() > 0) {
        caCert = cf.generateCertificate(bis) as? X509Certificate?
    }

    val tmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
    tmfStore.load(null, null)
    tmfStore.setCertificateEntry("cert-certificate", caCert)

    val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    tmf.init(tmfStore)

    // --------------------------------------------------------------------------------------------

    bis = BufferedInputStream(crtFile)
    var cert: X509Certificate? = null
    while (bis.available() > 0) {
        cert = cf.generateCertificate(bis) as? X509Certificate?
    }

    val key = JcaPEMKeyConverter().setProvider("BC")
        .getKeyPair(PEMParser(InputStreamReader(keyFile)).readObject() as PEMKeyPair)

    val kmfStore = KeyStore.getInstance(KeyStore.getDefaultType())
    kmfStore.load(null, null)
    kmfStore.setCertificateEntry("certificate", cert)
    kmfStore.setKeyEntry(
        "private-cert",
        key.private,
        password.toCharArray(),
        arrayOf<Certificate?>(cert)
    )

    val kmf: KeyManagerFactory =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())

    kmf.init(kmfStore, password.toCharArray())

    val context = SSLContext.getInstance("TLSv1.2")
    context.init(kmf.keyManagers, tmf.trustManagers, null)

    return context.socketFactory
}
