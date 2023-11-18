package daemon.dev.field.network.util

import android.util.Log
import daemon.dev.field.PEER_NET
import daemon.dev.field.network.Socket


class PeerNetwork {

    private var active_connections = hashMapOf<String,MutableList<Socket>>()

    fun size() = active_connections.size

    fun peers() = active_connections.keys

    fun add(socket : Socket) {
        Log.d(PEER_NET,"add() called ${socket.key}, ${socket.type2String()}")

        if(socket.key in active_connections.keys){

            val socks = active_connections[socket.key]!!

            for(s in socks){
                if(s.type == socket.type){
                    Log.d(PEER_NET,"Found peer with same type socket replacing")
                    socks.remove(s);
                    break
                }
            }

            socks.add(socket)
            active_connections[socket.key] = socks

        }else{

            active_connections[socket.key] = mutableListOf(socket)

        }

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
        Log.i(PEER_NET,"closeSocket() called ${socket.key}, ${socket.type2String()}")

        val key = socket.key
        val type = socket.type

        get(key,type)?.let{
            it.close()
            active_connections[key]!!.remove(it)
            if(active_connections[key]!!.isEmpty()){
                active_connections.remove(key)
            }
        } ?: run {socket.close()}

        return if(active_connections[key] == null){ 0 }else{ active_connections[key]!!.size }
    }

    fun clear() {

        for(socks in active_connections.values){
            for(s in socks){
                s.close()
            }
        }

        active_connections.clear()
    }

    fun contains(key : String) : Boolean{
        return active_connections[key] != null
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

        var out = ""

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