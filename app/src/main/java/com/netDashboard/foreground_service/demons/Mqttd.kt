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

    val isEnabled: Boolean
        get() = d.mqttEnabled

    var client = MqttAndroidClientExtended(context, d.mqttURI, Random.nextInt().toString())
    var conHandler = ConnectionHandler()

    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    init {
        conHandler.dispatch("init")
    }

    fun reinit() {
        if (client.isConnected) topicCheck()
        conHandler.dispatch("reinit")
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

    inner class ConnectionHandler {

        var isDone = MutableLiveData(false)

        private var _isDone = false
            set(value) {
                if (value != field) isDone.postValue(value)
                field = value
            }

        private var isDispatchScheduled = false

        fun dispatch(reason: String) {
            val sameOptions = client.serverURI == d.mqttURI &&
                    client.options.userName ?: "" == d.mqttUserName &&
                    client.options.password.contentEquals(d.mqttPass.toCharArray())

            _isDone = client.isConnected == isEnabled && (!isEnabled || sameOptions)

            Log.i("OUY", "DISPATCH($reason) isDone($_isDone) sameOptions($sameOptions)")

            if (!_isDone && !isDispatchScheduled) {
                isDone.postValue(_isDone)
                isDispatchScheduled = true

                val retry = if (isEnabled) {
                    if (!client.isClosed && !sameOptions) {
                        Log.i("OUY", "disconnectAttempt(short)")
                        client.disconnectAttempt(true); 300L
                    } else {
                        Log.i("OUY", "connectAttempt(long)")
                        client.connectAttempt(); 3000L
                    }
                } else {
                    Log.i("OUY", "disconnectAttempt(long)")
                    client.disconnectAttempt(); 3000L
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    isDispatchScheduled = false
                    dispatch("internal")
                }, retry)
            }
        }
    }

    inner class MqttAndroidClientExtended(
        context: Context,
        serverURI: String,
        clientId: String
    ) : MqttAndroidClient(context, serverURI, clientId) {

        private var isBusy = false
        var isClosed = true
        var topics: MutableList<Topic> = mutableListOf()
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

            if (client.isClosed) {
                client = MqttAndroidClientExtended(
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
                    conHandler.dispatch("con_lost")
                }

                override fun deliveryComplete(token: IMqttDeliveryToken?) {
                }
            })

            options.isCleanSession = true

            d.mqttPass.let {
                options.password = if (it.isNotBlank()) it.toCharArray() else null
            }

            d.mqttUserName.let {
                options.userName = if (it.isNotBlank()) it else null
            }

            try {
                client.connect(options, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        topicCheck()
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