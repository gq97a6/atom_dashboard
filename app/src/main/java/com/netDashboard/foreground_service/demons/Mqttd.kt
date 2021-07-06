package com.netDashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.random.Random

class Mqttd(private val context: Context, private val URI: String) : Daemon() {

    var isEnabled = false

    var client = MqttAndroidClientExtended(context, URI, Random.nextInt().toString())
    var conHandler = ConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    fun start() {
        if (isEnabled) return

        isEnabled = true
        conHandler.dispatch()
    }

    fun stop() {
        if (!isEnabled) return

        isEnabled = false
        conHandler.dispatch()
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {

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

    fun subscribe(topic: String, qos: Int = 1) {

        if (!client.isConnected) return

        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unsubscribe(topic: String) {

        if (!client.isConnected) return

        try {
            client.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    inner class ConnectionHandler(private val retryDelay: Long = 3000) {

        private var isDispatched = false
        var isDone = MutableLiveData(false)

        fun dispatch(force: Boolean = false) {

            if (isDispatched && !force) return
            isDispatched = true

            if (client.isConnected == isEnabled) {
                isDispatched = false
                isDone.postValue(true)
                isDone.value = false
                return
            }

            if (isEnabled) client.start() else client.stop()

            Handler(Looper.getMainLooper()).postDelayed({
                dispatch(true)
            }, retryDelay)
        }
    }

    inner class MqttAndroidClientExtended(
        context: Context,
        serverURI: String,
        clientId: String
    ) : MqttAndroidClient(context, serverURI, clientId) {

        private var isBusy = false

        fun start() {
            if (isBusy) return

            isBusy = true

            client = MqttAndroidClientExtended(context, URI, Random.nextInt().toString())

            client.setCallback(object : MqttCallback {

                override fun messageArrived(t: String?, m: MqttMessage) {
                    data.postValue(Pair(t ?: "", m))
                    data.value = Pair(null, null)
                }

                override fun connectionLost(cause: Throwable?) {
                    conHandler.dispatch()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            val options = MqttConnectOptions()

            try {
                client.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {}

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }

            isBusy = false
        }

        fun stop() {
            if (isBusy) return

            isBusy = true

            client.unregisterResources()
            client.close()

            try {
                client.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }

            client.setCallback(null)
            isBusy = false
        }
    }
}