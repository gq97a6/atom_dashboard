package com.alteratom.dashboard.daemon.daemons.mqttd

import androidx.lifecycle.MutableLiveData
import com.hivemq.client.mqtt.MqttClientState
import com.hivemq.client.mqtt.MqttClientTransportConfig
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client
import kotlinx.coroutines.*
import java.net.InetSocketAddress

//Server client class
class Mqtt3Server(
    private var daemon: Mqttd
) {

    var client: Mqtt3AsyncClient = Mqtt3Client.builder().buildAsync()
    var currentConfig = MqttConfig()
    var isStable: MutableLiveData<Boolean> = MutableLiveData(true)

    private fun connectAttempt() {
        val config = daemon.d.mqtt.copy()
        val transportConfig = MqttClientTransportConfig.builder()
            .serverAddress(InetSocketAddress(config.address, config.port))
            .build()

        client = Mqtt3Client.builder()
            .identifier(config.clientId)
            .transportConfig(transportConfig)
            .addDisconnectedListener {
                run {}
                isStable.postValue(true)
            }
            .addConnectedListener {
                currentConfig = config
                isStable.postValue(true)
            }
            .buildAsync()

        client.state
        client.connectWith()
            .simpleAuth()
            .username(config.username)
            .password(config.pass.toByteArray())
            .applySimpleAuth()
            .keepAlive(30)
            .cleanSession(true)
            .send()
    }

    private fun disconnectAttempt() = client.disconnect()

    //------------------------------------------------------------------------------------------

    private var dispatchJob: Job? = null

    @OptIn(DelicateCoroutinesApi::class)
    fun dispatch(cancel: Boolean = false) {
        if (cancel) {
            dispatchJob?.cancel()
        }

        //Return if already dispatched
        if (dispatchJob != null && dispatchJob?.isActive == true) {
            return
        }

        dispatchJob = GlobalScope.launch(Dispatchers.IO) {
            try {
                isStable.postValue(false)
                //withTimeout(10000) { handleDispatch() }
                handleDispatch()
                isStable.postValue(true)
            } catch (e: Exception) { //Create another coroutine after a delay
                e.printStackTrace()
                delay(1000)
                dispatch(true)
            }
        }
    }

    private suspend fun handleDispatch() {
        while (true) {
            if (daemon.d.mqtt.isEnabled && !daemon.isDischarged) when (client.state) {
                MqttClientState.CONNECTED -> { //check correct config and disconnect if wrong
                    if (currentConfig == daemon.d.mqtt) break
                    else disconnectAttempt()
                }
                MqttClientState.DISCONNECTED -> { //set config and connect
                    connectAttempt()
                }
                MqttClientState.CONNECTING -> {}
                MqttClientState.DISCONNECTED_RECONNECT -> {}
                MqttClientState.CONNECTING_RECONNECT -> {}
            } else when (client.state) {
                MqttClientState.CONNECTED -> disconnectAttempt()
                MqttClientState.DISCONNECTED -> break
                MqttClientState.CONNECTING -> {} //recheck
                MqttClientState.DISCONNECTED_RECONNECT -> {} //recheck
                MqttClientState.CONNECTING_RECONNECT -> {} //recheck
            }

            delay(1000)
        }
    }

    //val sslConfig = MqttClientSslConfig.builder()
    //.mqttConnectTimeout(10000, TimeUnit.MILLISECONDS)
    //.socketConnectTimeout(10000, TimeUnit.MILLISECONDS)
    //.webSocketConfig(ws)
    //.sslConfig(sslConfig)

    //client.subscribeWith()
    //.topicFilter("gda_switch0s")
    //.qos(MqttQos.AT_LEAST_ONCE)
    //.callback { x: Mqtt5Publish? -> println(x) }
    //.send()

    //client.publishWith()
    //.topic("gda_switch0s")
    //.qos(MqttQos.AT_MOST_ONCE)
    //.payload("1".toByteArray())
    //.retain(true)
    //.send()
}