package com.netDashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.netDashboard.dashboard.Dashboard
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class Mqttd(private val context: Context, var d: Dashboard) : Daemon() {

    var isEnabled = true
        get() = d.mqttEnabled && field

    var client = MqttAndroidClientExtended(context, d.mqttURI, d.mqttClientId)
    var conHandler = MqttdConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        client.isClosed = true
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

    private fun subscribe(topic: String, qos: Int) {

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

    private fun unsubscribe(topic: String) {

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

    fun topicCheck() {
        val topics: MutableList<Pair<String, Int>> = mutableListOf()
        for (tile in d.tiles) {
            for (t in tile.mqttData.subs) {
                Pair(t.value, tile.mqttData.qos).let {
                    if (!topics.contains(it) && t.value.isNotBlank()) {
                        topics.add(it)
                    }
                }
            }
        }

        val unsubTopics = client.topics - topics.toSet()
        val subTopics = topics - client.topics.toSet()

        for (t in unsubTopics) unsubscribe(t.first)
        for (t in subTopics) subscribe(t.first, t.second)

        client.topics = topics
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
            val sameOptions = client.serverURI == d.mqttURI &&
                    client.options.userName == d.mqttUserName &&
                    client.options.password.contentEquals(d.mqttPass?.toCharArray())

            _isDone = client.isConnected == isEnabled && (!isEnabled || sameOptions)

            if (!_isDone && !isDispatchScheduled) {
                isDone.postValue(_isDone)
                isDispatchScheduled = true

                if (isEnabled) {
                    if (client.isConnected) {
                        client.closeAttempt()
                    } else if (!sameOptions) {
                        client = MqttAndroidClientExtended(
                            context,
                            d.mqttURI,
                            d.mqttClientId
                        )
                        client.connectAttempt()
                    } else {
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
        serverURI: String,
        clientId: String
    ) : MqttAndroidClient(context, serverURI, clientId) {

        private var isBusy = false
        var isClosed = false
        var topics: MutableList<Pair<String, Int>> = mutableListOf()
        var options = MqttConnectOptions()

        override fun isConnected(): Boolean {
            return try {
                super.isConnected()
            } catch (e: Exception) {
                false
            }
        }

        fun connectAttempt() {
            if (isBusy) return
            isBusy = true

            options.isCleanSession = true
            options.userName = d.mqttUserName
            options.password = d.mqttPass?.toCharArray()

            setCallback(object : MqttCallback {
                override fun messageArrived(t: String?, m: MqttMessage) {
                    for (tile in d.tiles) tile.receive(Pair(t ?: "", m))
                    data.postValue(Pair(t ?: "", m))
                    data.value = Pair(null, null)
                }

                override fun connectionLost(cause: Throwable?) {
                    conHandler.dispatch("con_lost")
                    d.log.newEntry("MQTTD lost connection")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            try {
                connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                        d.log.newEntry("MQTTD connected")
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

            isBusy = false
        }

        fun disconnectAttempt(toClose: Boolean = false) {
            if (isBusy || isClosed) return
            isBusy = true

            try {
                client.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        unregisterResources()
                        setCallback(null)
                        topics = mutableListOf()

                        if (toClose) {
                            close()
                            isClosed = true
                        }

                        d.log.newEntry("MQTTD disconnected")
                    }

                    override fun onFailure(
                        asyncActionToken: IMqttToken?,
                        exception: Throwable?
                    ) {
                        d.log.newEntry("MQTTD failed to disconnect")
                    }
                })
            } catch (e: MqttException) {
                e.printStackTrace()
            }

            isBusy = false
        }

        fun closeAttempt() {
            disconnectAttempt(true)
        }
    }
}