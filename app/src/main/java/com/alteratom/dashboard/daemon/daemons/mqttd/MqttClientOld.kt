package com.alteratom.dashboard.daemon.daemons.mqttd

//Server client class
//class MqttClient(
//    context: Context,
//    var server: Mqttd,
//    var conProp: MqttConfig = server.d.mqtt.copy()
//) : MqttAndroidClient(context, conProp.uri, conProp.clientId) {
//
//    var topics: MutableList<Pair<String, Int>> = mutableListOf()
//    var isClosed = false
//
//    override fun isConnected() = try {
//        super.isConnected()
//    } catch (e: Exception) {
//        false
//    }
//
//    override fun removeMessage(token: IMqttDeliveryToken?): Boolean = false
//    override fun getInFlightMessageCount(): Int = 0
//    override fun reconnect() {}
//
//    fun connectAttempt() {
//        setCallback(object : MqttCallback {
//            override fun messageArrived(t: String?, m: MqttMessage) {
//                for (tile in server.d.tiles) tile.receive(Pair(t ?: "", m))
//                server.data.postValue(Pair(t ?: "", m))
//            }
//
//            override fun connectionLost(cause: Throwable?) {
//                topics = mutableListOf()
//                server.manager.dispatch("lost")
//                server.d.log.newEntry("lost: ${cause?.message + "||" + cause?.cause}")//TODO: remove
//            }
//
//            override fun deliveryComplete(token: IMqttDeliveryToken?) {
//            }
//        })
//
//        val options = MqttConnectOptions()
//
//        options.isCleanSession = true
//        options.keepAliveInterval = 30
//
//        if (conProp.includeCred) {
//            options.userName = conProp.username
//            options.password = conProp.pass.toCharArray()
//        } else {
//            options.userName = ""
//            options.password = charArrayOf()
//        }
//
//        if (conProp.ssl) setupSSL(options)
//
//        connect(options, null, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                server.topicCheck()
//                server.manager.dispatch("success")
//                server.d.log.newEntry("con_ok")//TODO: remove
//            }
//
//            override fun onFailure(
//                asyncActionToken: IMqttToken?,
//                exception: Throwable?
//            ) {
//                server.d.log.newEntry("con_fail")//TODO: remove
//            }
//        })
//    }
//
//    fun disconnectAttempt(close: Boolean = false) {
//        server.server.disconnect(null, object : IMqttActionListener {
//            override fun onSuccess(asyncActionToken: IMqttToken?) {
//                unregisterResources()
//                setCallback(null)
//                topics = mutableListOf()
//                server.d.log.newEntry("disc_ok")//TODO: remove
//            }
//
//            override fun onFailure(
//                asyncActionToken: IMqttToken?,
//                exception: Throwable?
//            ) {
//                server.d.log.newEntry("disc_fail")//TODO: remove
//            }
//        })
//
//        if (close) {
//            close()
//            isClosed = true
//        }
//    }
//
//