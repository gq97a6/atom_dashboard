package com.alteratom.dashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.fasterxml.jackson.annotation.JsonIgnore
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
import kotlin.random.Random

class Mqttd(private val context: Context) : Daemon() {

    var conProp = ConnectionProperties()
        set(value) {
            field = value
            notifyOptionsChanged()
        }

    @JsonIgnore
    var conHandler = ConnectionHandler()

    @JsonIgnore
    var client = MqttAndroidClientExtended(context, conProp.copy())

    @JsonIgnore
    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    override var isEnabled
        get() = conProp.isEnabled
        set(value) {
            conProp.isEnabled = value
        }

    override fun initialize() {
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
        for (tile in ds.getTiles().filter { it.mqtt.isEnabled }) {
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

    inner class ConnectionHandler {

        var isDone = MutableLiveData(false)

        private var _isDone = false
            set(value) {
                if (value != field) isDone.postValue(value)
                field = value
            }

        private var isDispatchScheduled = false

        fun dispatch(reason: String) {

            _isDone = client.isConnected == isEnabled && (client.conProp == conProp || !isEnabled)

            if (!_isDone && !isDispatchScheduled) {
                isDone.postValue(_isDone)
                isDispatchScheduled = true

                if (isEnabled) {
                    if (client.isConnected) client.closeAttempt()
                    else {
                        if (client.conProp.clientId != conProp.clientId || client.conProp.URI != conProp.URI)
                            client = MqttAndroidClientExtended(context, conProp.copy())

                        client.conProp = conProp.copy()
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
        var conProp: ConnectionProperties
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
                    for (tile in ds.getTiles()) tile.receive(Pair(t ?: "", m))
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
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
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

    data class ConnectionProperties(
        var isEnabled: Boolean = false,
        var ssl: Boolean = false,
        var sslTrustAll: Boolean = false,
        @JsonIgnore
        var sslCert: X509Certificate? = null,
        var sslFileName: String = "",
        var address: String = "tcp://",
        var port: Int = 1883,
        var includeCred: Boolean = false,
        var username: String = "",
        var pass: String = "",
        var clientId: String = kotlin.math.abs(Random.nextInt()).toString(),
    ) {
        val URI
            get() = "$address:$port"

        var sslCertStr: String? = null
            set(value) {
                field = value
                sslCert = try {
                    val cf = CertificateFactory.getInstance("X.509")
                    cf.generateCertificate(sslCertStr?.byteInputStream()) as X509Certificate
                } catch (e: Exception) {
                    null
                }
            }
    }
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
