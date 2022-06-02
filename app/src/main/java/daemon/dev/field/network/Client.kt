package daemon.dev.field.network

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.SERVER_PORT
import java.io.IOException
import java.net.InetAddress
import java.net.Socket


class Client(private val address : InetAddress) : Thread() {

    private val TAG = "ClientSocketHandler"
    private var socket: Socket? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun run(){
        Log.d("Client","created")

        try {

            socket = Socket(address, SERVER_PORT)
            //val peer = Peer(Peer.WIFI,socket!!,null,null)
            Log.d("Client","new peer")

        } catch (e: IOException) {

            Log.d("Client","signaling autoConnect...")

            e.printStackTrace()
            try {
                socket?.close()
            } catch (e1: IOException) {
                e1.printStackTrace()
            }
            return

        }

    }

}