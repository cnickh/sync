package daemon.dev.field.network

import android.util.Log
import daemon.dev.field.SERVER_PORT
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.UnknownHostException

class Host() : Thread() {

    private var serverSocket: ServerSocket? = null
    private var shouldLoop : Boolean = true

    override fun run(){
        Log.d("Host", "created")

        try {
            serverSocket = ServerSocket()
            serverSocket!!.bind(InetSocketAddress(SERVER_PORT))

        } catch (e: UnknownHostException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
        while(shouldLoop) {
//            try {
//
//                val peer = Peer(Peer.WIFI,serverSocket!!.accept(),null)
//                Log.d("Host", "Resolved new peer")
//
//                val connectionThread = Thread(peer)
//                connectionThread.start()
//
//            } catch (e: IOException) {
//
//                try {
//                    if (serverSocket != null && !serverSocket!!.isClosed) serverSocket!!.close()
//                } catch (ioe: IOException) {
//
//                }
//                e.printStackTrace()
//
//            }
//        }

    }


    fun kill(){
        shouldLoop = false
        try {
            if (serverSocket != null && !serverSocket!!.isClosed) serverSocket!!.close()
        } catch (ioe: IOException) {

        }
    }


}}