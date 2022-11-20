package daemon.dev.field.network.util

import android.bluetooth.BluetoothDevice
import android.util.Log
import daemon.dev.field.MAX_PEERS
import daemon.dev.field.PEER_NET
import daemon.dev.field.network.Async
import daemon.dev.field.network.Async.INSYNC
import daemon.dev.field.network.Async.READY
import daemon.dev.field.network.Socket


class PeerNetwork {

    private var state : Int = READY
    private var active_connections = hashMapOf<String,MutableList<Socket>>()

    fun state() = state

    fun size() = active_connections.size

    fun peers() = active_connections.keys

    fun add(socket : Socket) : Boolean{
        Log.d(PEER_NET,"add() called ${socket.key}, ${socket.type2String()}")

        return if(state == INSYNC){

          false

        } else if(socket.key in active_connections.keys){

            val socks = active_connections[socket.key]!!

            for(s in socks){
                if(s.type == socket.type){ socks.remove(s);break }
            }

            socks.add(socket)
            active_connections[socket.key] = socks
            true

        }else{

            active_connections[socket.key] = mutableListOf(socket)
            if(active_connections.size == MAX_PEERS){state = INSYNC}
            true

        }


    }

    fun getAny(key : String) : Socket? {
        return active_connections[key]?.let{ it[0] }
    }

    fun get(key : String, type : Int) : Socket?{
        active_connections[key]?.let{
            for(s in it){
                if(s.type == type){return s}
            }
        }
        return null
    }

    fun closeSocket(socket : Socket) : Int{
        Log.i(PEER_NET,"remove() called ${socket.key}, ${socket.type2String()}")

        val key = socket.key
        val type = socket.type

        get(key,type)?.let{
            it.close()
            active_connections[key]!!.remove(it)
            if(active_connections[key]!!.isEmpty()){
                active_connections.remove(key)
                state = READY
            }
        }

        return if(active_connections[key] == null){ 0 }else{ active_connections[key]!!.size }
    }

    fun removePeer(key : String) : Boolean {
        val ret = active_connections[key] != null
        active_connections[key]?.let{
            for(s in it){s.close()}
        }
        active_connections.remove(key)
        state = READY
        return ret
    }

    fun clear() {

        for((key,socks) in active_connections){
            for(s in socks){
                s.close()
            }
        }

        active_connections.clear()
    }

    fun contains(key : String) : Boolean{
        return active_connections[key] != null
    }

    fun getSocket(device : BluetoothDevice) : Socket?{
        var ret : Socket? = null

        for(key in active_connections.keys){
            for(socket in active_connections[key]!!){
                if(socket.type == Socket.BLUETOOTH_DEVICE){
                    if(socket.device.address == device.address){
                        ret = socket
                        break
                    }
                }
            }
        }

        return ret
    }

    fun gattConnection(key : String) : Socket? {
        active_connections[key]?.let{
            for (s in it){
                if(s.type == Socket.BLUETOOTH_GATT){return s}
            }
        }
        return null
    }

    fun print_state() {

        var out = "State == ${Async.state2String()}\n "

        for((k,sockets) in active_connections){
            var line = "$k ["
            for(s in sockets){
                line += s.type2String() + ", "
            }
            line += "]\n"
            out += line
        }

        Log.v(PEER_NET,out)
    }
}