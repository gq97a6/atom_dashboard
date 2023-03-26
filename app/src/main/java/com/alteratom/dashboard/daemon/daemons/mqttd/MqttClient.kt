package com.alteratom.dashboard.daemon.daemons.mqttd

import android.annotation.SuppressLint
import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import javax.net.ssl.*

//Server client class
class MqttClient(
    context: Context,
    var server: Mqttd,
    var conProp: MqttConfig = server.d.mqtt.copy()
) : MqttAndroidClient(context, conProp.uri, conProp.clientId) {

    var topics: MutableList<Pair<String, Int>> = mutableListOf()
    var isClosed = false

    override fun isConnected() = try {
        super.isConnected()
    } catch (e: Exception) {
        false
    }

    override fun removeMessage(token: IMqttDeliveryToken?): Boolean = false
    override fun getInFlightMessageCount(): Int = 0
    override fun reconnect() {}

    fun connectAttempt() {
        setCallback(object : MqttCallback {
            override fun messageArrived(t: String?, m: MqttMessage) {
                for (tile in server.d.tiles) tile.receive(Pair(t ?: "", m))
                server.data.postValue(Pair(t ?: "", m))
            }

            override fun connectionLost(cause: Throwable?) {
                topics = mutableListOf()
                server.manager.dispatch("lost")
                server.d.log.newEntry("lost")//TODO: remove
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

        if (conProp.ssl) setupSSL(options)

        connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                server.topicCheck()
                server.manager.dispatch("success")
                server.d.log.newEntry("con_ok")//TODO: remove
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                server.d.log.newEntry("con_fail")//TODO: remove
            }
        })
    }

    fun disconnectAttempt(close: Boolean = false) {
        server.client.disconnect(null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                unregisterResources()
                setCallback(null)
                topics = mutableListOf()
                server.d.log.newEntry("disc_ok")//TODO: remove
            }

            override fun onFailure(
                asyncActionToken: IMqttToken?,
                exception: Throwable?
            ) {
                server.d.log.newEntry("disc_fail")//TODO: remove
            }
        })

        if (close) {
            close()
            isClosed = true
        }
    }

    private fun setupSSL(options: MqttConnectOptions) {
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

        val trustManager = if (!conProp.sslTrustAll) { //TRUST ONLY IMPORTED
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
        }

        val tlsContext = SSLContext.getInstance("TLS")
        tlsContext.init(kmf.keyManagers, trustManager, java.security.SecureRandom())

        options.socketFactory = tlsContext.socketFactory
    }
}