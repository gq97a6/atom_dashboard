package com.alteratom.dashboard.daemon.daemons.mqttd

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.ConnectionManager
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttMessage

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    var manager = Manager()
    var client: MqttClient = MqttClient(context, this)
    var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val isDone: MutableLiveData<Boolean>
        get() = manager.isDone

    override val status: Status
        get() = if (!isEnabled) Status.DISCONNECTED
        else {
            if (client.isConnected)
                if (d.mqtt.ssl && !d.mqtt.sslTrustAll) Status.CONNECTED_SSL
                else Status.CONNECTED
            else if (manager.isDone.value != true) Status.ATTEMPTING
            else Status.FAILED
        }

    override fun notifyAssigned() {
        super.notifyAssigned()
        manager.dispatch("assign")
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        if (client.isConnected) manager.dispatch("discharge")
        else client.unregisterResources()
    }

    override fun notifyConfigChanged() {
        manager.dispatch("change")
        if (client.isConnected && isEnabled) topicCheck()
    }

    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = false) {
        if (!client.isConnected) return
        try {
            val message = MqttMessage().apply {
                payload = msg.toByteArray()
                this.qos = qos
                isRetained = retained
            }
            client.publish(topic, message, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun subscribe(topic: String, qos: Int) {
        if (!client.isConnected) return
        try {
            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    client.topics.add(Pair(topic, qos))
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                }
            })
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //Manage subscriptions at topic list change
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

    //Class that manages server client class
    inner class Manager : ConnectionManager() {
        override fun isDoneCheck(): Boolean =
            client.isConnected == isEnabled && (client.conProp == d.mqtt || !isEnabled)

        override fun handleDispatch(reason: String) {
            if (isEnabled) {
                if (client.isConnected || reason == "success") {
                    d.log.newEntry("close")//TODO: remove
                    client.disconnectAttempt(true)
                }
                else {
                    if (client.conProp.clientId != d.mqtt.clientId ||
                        client.conProp.uri != d.mqtt.uri ||
                        client.isClosed
                    ) {
                        client = MqttClient(context, this@Mqttd)
                        d.log.newEntry("new_client")//TODO: remove
                    }
                    else client.conProp = d.mqtt.copy()

                    d.log.newEntry("con")//TODO: remove
                    client.connectAttempt()
                }
            } else if (!client.isClosed || client.isConnected) {
                d.log.newEntry("close")//TODO: remove
                client.disconnectAttempt(true)
            }
        }
    }

    enum class Status { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}

//val sslConfig = MqttClientSslConfig.builder()
//.mqttConnectTimeout(10000, TimeUnit.MILLISECONDS)
//.socketConnectTimeout(10000, TimeUnit.MILLISECONDS)
//.webSocketConfig(ws)
//.sslConfig(sslConfig)
//val use5 = false
//
//val transportConfig = MqttClientTransportConfig.builder()
//    .serverAddress(InetSocketAddress("alteratom.com", 1883))
//    .build()
//
//val client = MqttClient.builder()
//    .identifier(UUID.randomUUID().toString())
//    .transportConfig(transportConfig)
//    .addDisconnectedListener {
//        println("disconnected!")
//    }
//    .addConnectedListener {
//        println("connected!")
//    }
//    .useMqttVersion5()
//    .buildAsync()
//
//client.connectWith()
//.simpleAuth()
//.username("user")
//.password("abc".toByteArray())
//.applySimpleAuth()
//.send()
//
//client.subscribeWith()
//.topicFilter("gda_switch0s")
//.qos(MqttQos.AT_LEAST_ONCE)
//.callback { x: Mqtt5Publish? -> println(x) }
//.send()
//
//runBlocking {
//    client.publishWith()
//        .topic("gda_switch0s")
//        .qos(MqttQos.AT_MOST_ONCE)
//        .payload("1".toByteArray())
//        .retain(true)
//        .send()
//
//    delay(3000)
//
//    client.publishWith()
//        .topic("gda_switch0s")
//        .qos(MqttQos.AT_LEAST_ONCE)
//        .payload("1".toByteArray())
//        .retain(false)
//        .send()
//
//    delay(3000)
//
//    client.publishWith()
//        .topic("gda_switch0s")
//        .qos(MqttQos.EXACTLY_ONCE)
//        .payload("1".toByteArray())
//        .retain(true)
//        .send()
//}