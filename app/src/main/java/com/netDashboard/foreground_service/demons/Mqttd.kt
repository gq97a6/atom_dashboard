package com.netDashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class Mqttd(private val context: Context, private val URI: String) : Daemon() {

    val isConnected
        get() = client?.isConnected ?: false

    var onConnect = MutableLiveData(false)

    private var isReady = false
        get() = client != null && field

    private var isEnabled = false
    private var isClientBusy = false

    private var client: MqttAndroidClient? = null

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        start()
    }

    private fun start() {
        if (isEnabled) return

        isEnabled = true
        toggleLoop()
    }

    fun stop() {
        if (!isEnabled) return

        isEnabled = false
        toggleLoop()
    }

    private fun toggleLoop(d: Long = 3000) {
        if (isConnected == isEnabled) return

        if (isEnabled) {
            startRaw()
        } else {
            stopRaw()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            toggleLoop()
        }, d)
    }

    private fun startRaw() {
        if (isClientBusy) return

        Log.i("OUY", "trying to connect to: $URI")

        isClientBusy = true

        client = MqttAndroidClient(context, URI, "kotlin_client")
        client?.setCallback(object : MqttCallback {

            override fun messageArrived(t: String?, m: MqttMessage) {
                data.postValue(Pair(t ?: "", m))
                Log.i("OUY", m.toString())
            }

            override fun connectionLost(cause: Throwable?) {
                toggleLoop()
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
            }
        })

        val options = MqttConnectOptions()

        try {
            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    onConnect.postValue(true)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        isReady = true
        isClientBusy = false
    }

    private fun stopRaw() {
        if (!isReady || isClientBusy) return

        isClientBusy = true

        client?.unregisterResources()
        client?.close()

        try {
            client?.disconnect(null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        client?.setCallback(null)
        client = null

        isReady = false
        isClientBusy = false
    }

    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false) {

        if (!isConnected) return

        try {
            val message = MqttMessage()

            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained

            client?.publish(topic, message, null, object : IMqttActionListener {
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

        if (!isConnected) return

        try {
            client?.subscribe(topic, qos, null, object : IMqttActionListener {
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

        if (!isConnected) return

        try {
            client?.unsubscribe(topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }
}