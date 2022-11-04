package daemon.dev.field.network

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.ConditionVariable
import android.os.Handler
import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.BLE_INTERVAL
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.cereal.objects.MeshRaw.Companion.DISCONNECT
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import daemon.dev.field.network.Socket.Companion.BLUETOOTH_DEVICE
import daemon.dev.field.network.util.Packer
import daemon.dev.field.network.util.SyncOperator
import daemon.dev.field.network.util.Verifier
import kotlinx.coroutines.sync.Mutex

/**@brief this object implements a singleton instance that preserves network state between
 * application and background service processes. It handles connections and disconnections with
 * remote devices. It also handle sending and receiving of network packets. The main purpose is too
 * manage connections and ensure accurate data about remote devices is delivered to application
 * processes. It implements thread safe code to ensure there are no race conditions when adding
 * or removing remote devices. */

object Async {
    const val MAX_CONNECTIONS = 3

    /**Possible States*/
    const val IDLE = 0
    const val READY = 1
    const val INSYNC = 2

    val live_state = MutableLiveData(IDLE)
    private var state = IDLE
    private val state_lock = Mutex()
    val response = ConditionVariable()

    private var num_connections = 0
    private var active_connections = hashMapOf<String,MutableList<Socket>>()
    private var message_number = 0

    private val _peers = mutableListOf<User>()
    val filter = MutableLiveData<Int>()
    val peers = MutableLiveData<MutableList<User>>(mutableListOf())
    val new_thread  = MutableLiveData<String>()
    val ping = MutableLiveData<String>()

    private lateinit var ds : SyncDatabase
    private lateinit var op : SyncOperator
    private lateinit var vr : Verifier
    private lateinit var nl : Handler
    private lateinit var sw : MeshService.NetworkSwitch

    suspend fun me() : User{
        return ds.userDao.wait(PUBLIC_KEY)!!
    }

    suspend fun state() : Int{
        state_lock.lock()
        val ret = state
        state_lock.unlock()
        return ret
    }

    suspend fun handshake() : HandShake {
        state_lock.lock()
        val shake = HandShake(state, me(), null)
        state_lock.unlock()

        return shake
    }

    suspend fun ready(context : Context, nl : Handler, switch : MeshService.NetworkSwitch){
        state_lock.lock()
        if(state != IDLE){state_lock.unlock();return}

        sw = switch
        this.nl = nl
        vr = Verifier()

        ds = SyncDatabase.getInstance(context)
        op = SyncOperator(PostRepository(ds.postDao), UserBase(ds.userDao), ChannelAccess(ds.channelDao))

        op.setLiveData(new_thread)
        op.setPing(ping)
        op.setVerifier(vr)
        op.setLiveFilter(filter)

        state = READY
        live_state.postValue(state)

        state_lock.unlock()
    }

    suspend fun checkKey(key : String) : Boolean {
        state_lock.lock()
        val ret = active_connections.containsKey(key)
        state_lock.unlock()
        return ret
    }


    suspend fun connect(socket : Socket, user : User) : Boolean{
        Log.d("Async","Calling connect on ${socket.key}")

        state_lock.lock()

        op.insertUser(user)

        if(socket.key in active_connections.keys){

            val socks = active_connections[socket.key]!!

            for(s in socks){
                if(s.type == socket.type){ socks.remove(s);break }
            }

            socks.add(socket)
        }else{
            if(state != READY){state_lock.unlock();return false}

            active_connections[socket.key] = mutableListOf(socket)
            _peers.add(socket.user)
            peers.postValue(_peers)
            num_connections++
            if(num_connections == MAX_CONNECTIONS){
                state = INSYNC;live_state.postValue(state)
                sw.off()
            }
            Sync.init_connection(user.key)
        }

        print_state()
        state_lock.unlock()
        return true
    }

    suspend fun getSocket(device : BluetoothDevice) : Socket?{
        var ret : Socket? = null
        state_lock.lock()
        if(state == IDLE){state_lock.unlock();return ret}

        for(key in active_connections.keys){
            for(socket in active_connections[key]!!){
                if(socket.type == BLUETOOTH_DEVICE){
                    if(socket.device.address == device.address){
                        ret = socket
                        break
                    }
                }
            }
        }

        state_lock.unlock()
        return ret
    }

    suspend fun disconnectSocket(socket : Socket){
        Log.i("Async.kt","disconnectSocket() called")


        state_lock.lock()

        socket.close()

        if(socket.type == Socket.BLUETOOTH_GATT){
            nl.obtainMessage(NetworkLooper.APP,
                NetworkLooper.AppEvent(socket)).sendToTarget()
        }

        active_connections[socket.key]?.let{

            it.remove(socket)
            if(it.size == 0){
                disconnectInsync(socket.user)
            }

        }
        print_state()
        state_lock.unlock()
    }

    suspend fun disconnect(user : User){
        val raw = MeshRaw(DISCONNECT, null, null, null, null, null)
        send(raw,user.key)

        val socks = active_connections[user.key]


        socks?.let{

            if(socks.size == 1){
                disconnectSocket(socks[0])
            }else if(socks.size == 2){
                val sock0 = socks[0]
                val sock1 = socks[1]
                disconnectSocket(sock0)
                disconnectSocket(sock1)

            }

        }

    }

    private fun disconnectInsync(user : User){

        active_connections.remove(user.key)
        num_connections--
        _peers.remove(user)
        peers.postValue(_peers)
        if(state == INSYNC){
            state = READY;live_state.postValue(state)
            sw.on()
        }

    }

    suspend fun send(raw : MeshRaw, key : String){
        active_connections[key]?.let{
            send(raw,it[0])
        }
    }

    suspend fun send(raw : MeshRaw, socket : Socket){
        state_lock.lock()
        if(state == IDLE){state_lock.unlock();return}

        if(raw.type != MeshRaw.CONFIRM) {
            raw.mid = message_number++
            Log.d("Async", "Sending ${type2string(raw.type)} mid ${raw.mid}")
        }else{
            Log.d("Async", "Sending ${type2string(raw.type)} for mid ${op.bytesFromBuffer(raw.misc!!)}")
        }
        val packer = Packer(raw)
        var buffer = packer.next()

        var count = 0
        while(buffer != null){
            Log.i("Async.kt", "sending packet $count / ${packer.count()}")

            socket.write(buffer)
            buffer = packer.next()

            if(!response.block(BLE_INTERVAL)){
                Log.e("Async.kt","response timeout for $count / ${packer.count()}")
            }

            response.close()
            count++
        }

        state_lock.unlock()


        if(raw.type != MeshRaw.CONFIRM){
            vr.add(socket, raw.mid)
        }

    }

    suspend fun sendAll(raw : MeshRaw){
        for(key in active_connections.keys) {
            active_connections[key]?.get(0)?.let { send(raw, it) }
        }
    }

    suspend fun receive(bytes : ByteArray, socket : Socket){
        if(state == IDLE){return}
        op.receive(bytes,socket)
    }

    suspend fun idle(){
        val raw = MeshRaw(DISCONNECT, null, null, null, null, null)
        sendAll(raw)

        state_lock.lock()
        if(state == IDLE){state_lock.unlock();return}

        for(p in _peers) {

            active_connections[p.key]?.let{
                for(socket in it){
                    socket.close()
                }
            }

            active_connections.remove(p.key)
        }
        _peers.clear()
        peers.postValue(_peers)
        num_connections = 0
        state = IDLE
        live_state.postValue(state)

        state_lock.unlock()
    }

    private fun print_state() {


        var out = "State == ${state2String()}\n "

        for((k,sockets) in active_connections){
            var line = "$k ["
            for(s in sockets){
                line += s.type2String() + ", "
            }
            line += "]\n"
            out += line
        }

        Log.v("Async.kt",out)
    }

    private fun state2String() : String{
        return when(state){
            IDLE ->{"IDLE"}
            READY->{"READY"}
            INSYNC->{"INSYNC"}
            else->{"ERROR STATE NO DEFINED"}
        }
    }

    private fun type2string(type : Int) : String{
        val mtype : String

        when(type){
            MeshRaw.INFO ->{
                mtype = "INFO"
            }

            MeshRaw.POST_LIST->{
                mtype = "POST_LIST"
            }

            MeshRaw.POST_W_ATTACH->{
                mtype = "POST_W_ATTACH"
            }

            MeshRaw.REQUEST -> {
                mtype = "REQUEST"
            }

            MeshRaw.NEW_DATA -> {
                mtype = "NEW_DATA"
            }

            MeshRaw.PING->{
                mtype = "PING"
            }

            DISCONNECT->{
                mtype = "DISCONNECT"
            }

            MeshRaw.CONFIRM->{
                mtype = "CONFIRM"
            }

            else ->{
                mtype = "NO_TYPE"
            }
        }

        return mtype

    }
}