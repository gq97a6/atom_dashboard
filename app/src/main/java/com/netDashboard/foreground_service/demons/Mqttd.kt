package com.netDashboard.foreground_service.demons

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.netDashboard.dashboard.Dashboard
import com.netDashboard.tile.Tile
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import kotlin.random.Random

class Mqttd(private val context: Context, private val dashboard: Dashboard) : Daemon() {

    val isEnabled
        get() = dashboard.mqttEnabled

    var client = MqttAndroidClientExtended(context, dashboard.mqttURI, Random.nextInt().toString())
    var conHandler = ConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        conHandler.decide()
    }

    fun reinit() {
        conHandler.decide()
        if (client.isConnected) topicCheck()
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

    private fun subscribe(topic: Tile.MqttTopics.TopicList.Topic) {

        if (!client.isConnected) return

        try {
            client.subscribe(topic.topic, topic.qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(
                        "OUY",
                        "${dashboard.dashboardTagName}: topic sub: ${topic.topic}:${topic.qos}"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun unsubscribe(topic: Tile.MqttTopics.TopicList.Topic) {

        if (!client.isConnected) return

        try {
            client.unsubscribe(topic.topic, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(
                        "OUY",
                        "${dashboard.dashboardTagName}: topic unsub: ${topic.topic}:${topic.qos}"
                    )
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    //todo: make it work
    fun topicCheck() {
        Log.i("OUY", "topicCheck")

        val mqttTopics: MutableList<Tile.MqttTopics.TopicList.Topic> = mutableListOf()
        for (t in dashboard.tiles) {
            for (topic in t.mqttTopics.subs.topics) {
                if (!mqttTopics.contains(topic)) mqttTopics.add(topic)
            }
        }

        Log.i("OUY", mqttTopics.size.toString())
        Log.i("OUY", client.topics.size.toString())

        for (topic in client.topics) {
            if (!mqttTopics.contains(topic)) {
                unsubscribe(topic)
            }
        }

        for (topic in mqttTopics) {
            if (!client.topics.contains(topic)) {
                subscribe(topic)
            }
        }

        client.topics = mqttTopics
    }

    inner class ConnectionHandler(private var retryDelay: Long = 3000) {

        private var isDispatched = false
            set(value) {
                isDone.postValue(!value)
                field = value
            }

        var isDone = MutableLiveData(true)

        private fun handleDispatch() {
            if (!isDispatched) return

            if (client.isConnected == isEnabled) {
                isDispatched = false
            }

            if (isEnabled) {
                if (client.serverURI == dashboard.mqttURI || client.isClosed) {
                    client.connectAttempt()
                } else {
                    client.disconnectAttempt(true)

                    Handler(Looper.getMainLooper()).postDelayed({
                        handleDispatch()
                    }, 300)
                }
            } else client.disconnectAttempt()

            Handler(Looper.getMainLooper()).postDelayed({
                handleDispatch()
            }, retryDelay)
        }

        fun decide() {
            if (client.isConnected != isEnabled || client.serverURI != dashboard.mqttURI) {
                if (!isDispatched) {
                    isDispatched = true
                    handleDispatch()
                }
            } else {
                isDispatched = false
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
        var topics: MutableList<Tile.MqttTopics.TopicList.Topic> = mutableListOf()

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

            if (client.isClosed || client.isClosed && client.serverURI != dashboard.mqttURI) {
                client =
                    MqttAndroidClientExtended(
                        context,
                        dashboard.mqttURI,
                        Random.nextInt().toString()
                    )
            }

            client.setCallback(object : MqttCallback {

                override fun messageArrived(t: String?, m: MqttMessage) {
                    for (tile in dashboard.tiles) tile.onData(Pair(t ?: "", m))
                    data.postValue(Pair(t ?: "", m))
                    data.value = Pair(null, null)
                }

                override fun connectionLost(cause: Throwable?) {
                    conHandler.decide()
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            val options = MqttConnectOptions()

            try {
                client.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
                        conHandler.decide()
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

                        conHandler.decide()
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