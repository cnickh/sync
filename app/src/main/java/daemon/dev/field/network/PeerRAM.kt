package daemon.dev.field.network

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.os.Build
import android.os.ConditionVariable
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.MANAGER_TAG
import daemon.dev.field.P2P_TAG
import daemon.dev.field.data.PostRAM
import daemon.dev.field.data.Server
import daemon.dev.field.data.objects.NodeInfo
import daemon.dev.field.nypt.Key
import daemon.dev.field.util.KeyMap
import kotlinx.coroutines.sync.Mutex

@RequiresApi(Build.VERSION_CODES.O)
object PeerRAM {

    val cLock : Mutex = Mutex()
    val res : ConditionVariable = ConditionVariable()

    private val PeerMap = KeyMap()

    /**User data*/
    private val userMap = KeyMap()
    val activeUsers = MutableLiveData<List<Key>>(listOf())

    private var server : Handler? = null
    private var resolver : Handler? = null
    private var gattServer : BluetoothGattServer? = null

    fun setGatt(gattServer: BluetoothGattServer?){
        this.gattServer = gattServer
    }

    fun setServer(handler: Handler?){
        server = handler
    }

    fun setResolver(handler : Handler?){
        resolver = handler
    }

    fun getResolver() : Handler {
        while(resolver == null) {}
        return resolver!!
    }

    fun getGatt() : BluetoothGattServer{
        while(gattServer == null) {}
        return gattServer!!
    }

    fun getServer() : Handler {
        while(server == null) {}
        return server!!
    }

    fun isResolver() : Boolean {
        return resolver == null
    }

    fun isGatt() : Boolean{
        return gattServer == null
    }

    fun isServer() : Boolean {
        return server == null
    }

    fun closeAll(){
        server = null
        resolver = null
        gattServer = null
    }

    suspend fun connect(socket : Socket) : Int{
        Log.d(P2P_TAG,"connect() called")

        val uid = socket.key

        cLock.lock()

        val sid : Int = if(PeerMap.get(socket.key)==null){
            PeerMap.put(uid, mutableListOf(socket))
        } else {
            val socket_list = PeerMap.get(socket.key) as MutableList<Socket>
            socket_list.add(socket)
            Log.d(P2P_TAG,"new socket_list = $socket_list")
            PeerMap.put(uid,socket_list)
        }

        server?.obtainMessage(0,
            Server.Request(sid,Server.INFO,null,null)
        )?.sendToTarget()

        cLock.unlock()

        return sid
    }

    suspend fun disconnectPeer(sid : Int){
        Log.d(P2P_TAG,"disconnectPeer() called")

        cLock.lock()

        val key = PeerMap.getKeyAt(sid)
        if(key==null){return}else{
            val socket_list = PeerMap.get(key) as MutableList<Socket>
            for (socket in socket_list){
                socket.close()
            }
            PeerMap.remove(key)
            dropNodeInfo(key)
        }

        cLock.unlock()
    }

    suspend fun disconnect(sid : Int, type : Int){
        Log.d(P2P_TAG,"disconnect() called")

        cLock.lock()

        val key = PeerMap.getKeyAt(sid)
        if(key==null){return}else{
            val socket_list = PeerMap.get(key) as MutableList<Socket>

            val cpy_list = socket_list.toMutableList()

            for (socket in socket_list){
                if(socket.type == type){
                    socket.close()
                    if(cpy_list.size == 1){
                        PeerMap.remove(key)
                        dropNodeInfo(key)
                        break
                    } else{
                        cpy_list.remove(socket)
                        Log.d(P2P_TAG,"new list: $cpy_list")
                        PeerMap.put(key,cpy_list)
                        break
                    }
                }
            }
        }

        cLock.unlock()

    }

    suspend fun disconnectAll(){
        val peerKeys = PeerMap.getKeys()

        for(peer in peerKeys){
            val sid = PeerMap.getKeyIndex(peer)
            getServer().obtainMessage(0,
                sid?.let { Server.Request(it, Server.DISCONNECT, null,null) }
            ).sendToTarget()
        }

        Thread.sleep(30L)

        for(peer in peerKeys){
            val sid = PeerMap.getKeyIndex(peer)
            sid?.let { disconnectPeer(it) }
        }

        activeUsers.postValue(listOf())
    }

    fun getConnectionBySid(sid : Int) : Socket{
        Log.d(P2P_TAG,"getAt(): ${PeerMap.getAt(sid)}")
        val socket_list = PeerMap.getAt(sid) as List<Socket>
        return socket_list[0]
    }

    fun getUserAtSock(sid : Int) : Key? {
        return PeerMap.getKeyAt(sid)
    }

    fun getSockOfUser(key : Key) : Int? {
        return PeerMap.getKeyIndex(key)
    }

    fun getDeviceSid(device : BluetoothDevice) : Int?{
        Log.d(P2P_TAG,"getMap(): ${PeerMap.getMap()}")

        for(socket_list in PeerMap.getMap() as List<MutableList<Socket>>) {
            for (socket in socket_list) {
                if (socket.type == Socket.BLUETOOTH_DEVICE) {
                    if (socket.device?.address == device.address) {
                        return socket.sid
                    }
                }
            }
        }

        return null
    }

    fun putNodeInfo(info : NodeInfo){
        userMap.put(info.user.uid,info)
        activeUsers.postValue(userMap.getKeys())
    }

    fun getNodeInfo(key : Key) : NodeInfo?{
        return userMap.get(key)?.let{it as NodeInfo }
    }

    private fun dropNodeInfo(key : Key){
        userMap.get(key)?.let{
            val alias =  (it as NodeInfo).user.alias
            Log.d(P2P_TAG,"user $alias removed")
            userMap.remove(key)
            activeUsers.postValue(userMap.getKeys())
        }

    }

}