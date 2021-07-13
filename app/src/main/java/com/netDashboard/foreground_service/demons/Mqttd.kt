package com.netDashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.tile.Tile.MqttTopics.TopicList.Topic
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.random.Random

class Mqttd(private val context: Context, private val d: Dashboard) : Daemon() {

    val isEnabled
        get() = d.mqttEnabled

    var client = MqttAndroidClientExtended(context, d.mqttURI, Random.nextInt().toString())
    var conHandler = ConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        conHandler.dispatch()
    }

    fun reinit() {
        if (client.isConnected) topicCheck()
        conHandler.dispatch()
        Log.i(
            "OUY",
            "reinit"
        )
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

    private fun subscribe(topic: Topic) {

        if (!client.isConnected) return

        try {
            client.subscribe(topic.topic, topic.qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(
                        "OUY",
                        "${d.dashboardTagName}: topic sub: ${topic.topic}:${topic.qos}"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun unsubscribe(topic: Topic) {

        if (!client.isConnected) return

        try {
            client.unsubscribe(topic.topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(
                        "OUY",
                        "${d.dashboardTagName}: topic unsubscribe: ${topic.topic}:${topic.qos}"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun topicCheck() {

        val topics: MutableList<Topic> = mutableListOf()
        for (tile in d.tiles) {
            for (t in tile.mqttTopics.subs.topics) {
                if (!topics.contains(t) && t.topic.isNotBlank()) {
                    topics.add(t.copy())
                }
            }
        }

        val unsubTopics = client.topics - topics
        val subTopics = topics - client.topics

        for (t in unsubTopics) unsubscribe(t)
        for (t in subTopics) subscribe(t)

        client.topics = topics
    }

    inner class ConnectionHandler(private var retryDelay: Long = 3000) {

        var isDone = MutableLiveData(false)

        private var _isDone = false
            set(value) {
                if (value != field) isDone.postValue(value)
                field = value
            }

        private var isDispatchScheduled = false

        private fun handleDispatch(): Long {
            if (isEnabled) {
                if (client.serverURI == d.mqttURI || client.isClosed) {
                    client.connectAttempt()
                } else {
                    client.disconnectAttempt(true)
                    return 300
                }
            } else client.disconnectAttempt()
            return 3000
        }

        fun dispatch() {
            _isDone =
                isEnabled == client.isConnected && (!isEnabled || (client.serverURI == d.mqttURI))

            if (!_isDone) {
                if (!isDispatchScheduled) {
                    isDone.postValue(_isDone)
                    isDispatchScheduled = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        isDispatchScheduled = false
                        dispatch()
                    }, handleDispatch())
                }
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
        var topics: MutableList<Topic> = mutableListOf()

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

            if (client.isClosed || client.isClosed && client.serverURI != d.mqttURI) {
                client =
                    MqttAndroidClientExtended(
                        context,
                        d.mqttURI,
                        Random.nextInt().toString()
                    )
            }

            client.setCallback(object : MqttCallback {

                override fun messageArrived(t: String?, m: MqttMessage) {
                    for (tile in d.tiles) tile.onData(Pair(t ?: "", m))
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
            options.isCleanSession = true

            try {
                client.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                        conHandler.dispatch()
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

        fun disconnectAttempt(close: Boolean = false) {
            if (isBusy || isClosed) return

            isBusy = true

            try {
                client.disconnect(null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        client.unregisterResources()
                        client.setCallback(null)

                        if (close) {
                            client.close()
                            isClosed = true
                        }

                        conHandler.dispatch()
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
    }
}

//mqttd.conHandler.isDone.observe(context as LifecycleOwner, { isDone ->
//    if (isDone) {
//        val list: MutableList<String> = mutableListOf()
//        for (tile in dashboard.tiles) {
//            for (topic in tile.mqttTopics.sub.get()) {
//                if (!list.contains(topic)) {
//                    mqttd.subscribe(topic)
//                    list.add(topic)
//                }
//            }
//        }
//    }
//})