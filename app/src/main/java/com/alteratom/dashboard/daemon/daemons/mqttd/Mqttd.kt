package com.alteratom.dashboard.daemon.daemons.mqttd

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.alteratom.dashboard.Dashboard
import com.alteratom.dashboard.daemon.Daemon

class Mqttd(context: Context, dashboard: Dashboard) : Daemon(context, dashboard) {

    private var server: Mqtt3Server = Mqtt3Server(this)
    //var data: MutableLiveData<Pair<String?, MqttMessage?>> = MutableLiveData(Pair(null, null))

    public override val isEnabled
        get() = d.mqtt.isEnabled && !isDischarged

    override val statePing: MutableLiveData<Nothing?>
        get() = server.statePing

    override val state: State
        get() = when (server.state) {
            Mqtt3Server.State.CONNECTED -> State.CONNECTED
            Mqtt3Server.State.CONNECTED_SSL -> State.CONNECTED_SSL
            Mqtt3Server.State.ATTEMPTING -> State.ATTEMPTING
            Mqtt3Server.State.DISCONNECTED -> State.DISCONNECTED
            Mqtt3Server.State.FAILED -> State.FAILED
        }

    //if (!isEnabled) Status.DISCONNECTED
    //else {
    //    if (server.isConnected)
    //        if (d.mqtt.ssl && !d.mqtt.sslTrustAll) Status.CONNECTED_SSL
    //        else Status.CONNECTED
    //    else if (manager.isDone.value != true) Status.ATTEMPTING
    //    else Status.FAILED
    //}

    override fun notifyAssigned() {
        super.notifyAssigned()
        //manager.dispatch("assign")
        server.dispatch()
    }

    override fun notifyDischarged() {
        super.notifyDischarged()
        //if (server.isConnected) manager.dispatch("discharge")
        //else server.unregisterResources()
        server.dispatch()
    }

    override fun notifyConfigChanged() {
        super.notifyConfigChanged()
        //manager.dispatch("change")
        //if (server.isConnected && isEnabled) topicCheck()
        server.dispatch()
    }

    fun publish(topic: String, msg: String, qos: Int = 0, retained: Boolean = false) {
        //if (!server.isConnected) return
        //try {
        //    val message = MqttMessage().apply {
        //        payload = msg.toByteArray()
        //        this.qos = qos
        //        isRetained = retained
        //    }
        //    server.publish(topic, message, null, object : IMqttActionListener {
        //        override fun onSuccess(asyncActionToken: IMqttToken?) {
        //        }
//
        //        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        //        }
        //    })
        //} catch (e: Exception) {
        //    e.printStackTrace()
        //}
    }

    private fun subscribe(topic: String, qos: Int) {
        //if (!server.isConnected) return
        //try {
        //    server.subscribe(topic, qos, null, object : IMqttActionListener {
        //        override fun onSuccess(asyncActionToken: IMqttToken?) {
        //            server.topics.add(Pair(topic, qos))
        //        }
//
        //        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        //        }
        //    })
        //} catch (e: Exception) {
        //    e.printStackTrace()
        //}
    }

    private fun unsubscribe(topic: String, qos: Int) {
        //if (!server.isConnected) return
        //try {
        //    server.unsubscribe(topic, null, object : IMqttActionListener {
        //        override fun onSuccess(asyncActionToken: IMqttToken?) {
        //            server.topics.remove(Pair(topic, qos))
        //        }
//
        //        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
        //        }
        //    })
        //} catch (e: Exception) {
        //    e.printStackTrace()
        //}
    }

    //Manage subscriptions at topic list change
    fun topicCheck() {
        //val topics: MutableList<Pair<String, Int>> = mutableListOf()
        //for (tile in d.tiles.filter { it.mqtt.isEnabled }) {
        //    for (t in tile.mqtt.subs) {
        //        Pair(t.value, tile.mqtt.qos).let {
        //            if (!topics.contains(it) && t.value.isNotBlank()) {
        //                topics.add(it)
        //            }
        //        }
        //    }
        //}
        //val unsubTopics = server.topics - topics.toSet()
        //val subTopics = topics - server.topics.toSet()
        //for (t in unsubTopics) unsubscribe(t.first, t.second)
        //for (t in subTopics) subscribe(t.first, t.second)
    }

    enum class State { DISCONNECTED, CONNECTED, CONNECTED_SSL, FAILED, ATTEMPTING }
}