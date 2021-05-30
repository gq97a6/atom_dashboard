package com.netDashboard.abyss.demons

//class Udpd(val context: Context, private var port: Int, dashboard: Dashboard) :
//    Thread() {
//
//    private var running = false
//
//    private lateinit var socket: DatagramSocket
//    val tiles = dashboard.tiles
//
//    private val buf = ByteArray(5000)
//    private var data = MutableLiveData("C9ZF56ZLF4EW5355")
//
//    override fun run() {
//        val packet = DatagramPacket(buf, buf.size)
//
//        try {
//            socket = DatagramSocket(null)
//            socket.reuseAddress = true
//            socket.bind(InetSocketAddress(port))
//        } catch (e: Exception) {
//            Log.d("OUY", e.toString())
//        }
//
//        val threadsMaxNum = 10
//
//        val threadsNum: Int
//        val tilesPerThread: Int
//
//        when {
//            threadsMaxNum < tiles.size -> {
//
//                threadsNum = threadsMaxNum
//
//                tilesPerThread = if (tiles.size % threadsMaxNum != 0) {
//                    (tiles.size - (tiles.size % threadsMaxNum)) / threadsNum
//                } else {
//                    tiles.size / threadsNum
//                }
//            }
//
//            else -> {
//                threadsNum = tiles.size
//                tilesPerThread = 1
//            }
//        }
//
//        while (running) {
//            try {
//                socket.receive(packet)
//                Log.i("OUY", "packet")
//                data.postValue(String(packet.data, 0, packet.length))
//
//                //for (t in 1 until threadsNum) {
////
//                //    Thread {
//                //        for (i in tilesPerThread * (t - 1) until tilesPerThread * t) {
//                //            tiles[i].onData("1", false)
//                //        }
//                //    }.start()
//                //}
////
//                //Thread {
//                //    for (i in tilesPerThread * (threadsNum - 1) until tiles.size) {
//                //        tiles[i].onData("1", false)
//                //    }
//                //}.start()
//
//                //socket.send(packet)
//
//                val threadSet: Set<Thread> = getAllStackTraces().keys
//                Log.i("OUY", threadSet.size.toString())
//            } catch (e: Exception) {
//                Log.d("OUY", e.toString())
//                return
//            }
//        }
//
//        socket.close()
//    }
//
//    override fun start() {
//        super.start()
//        Log.i("OUY", "START!")
//
//        running = true
//    }
//
//    fun kill() {
//        running = false
//    }
//
//    fun receive(): LiveData<String> {
//        return data
//    }
//
//
//    fun send(data: String, address: String, port: Int) {
//        //Hack Prevent crash (sending should be done using an async task)
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//
//        try {
//            val inetAddress = InetAddress.getByName(address)
//            val packet =
//                DatagramPacket(data.toByteArray(), data.toByteArray().size, inetAddress, port)
//
//            if (packet.port == this.port) {
//                this.socket.send(packet)
//            } else {
//                val socket = DatagramSocket(port)
//                socket.send(packet)
//                socket.close()
//            }
//        } catch (e: java.lang.Exception) {
//            Log.e("OUY", e.toString())
//        }
//    }
//}