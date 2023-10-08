package daemon.dev.field.network

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Handler
import android.os.SystemClock.elapsedRealtime
import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.ASYNC_TAG
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.*
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import daemon.dev.field.network.handler.event.APP
import daemon.dev.field.network.handler.event.AppEvent
import daemon.dev.field.network.util.Packer
import daemon.dev.field.network.util.PeerNetwork
import daemon.dev.field.network.util.SyncOperator
import daemon.dev.field.network.util.Verifier
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.HashMap

/**@brief this object implements a singleton instance that preserves network state between
 * application and background service processes. It handles connections and disconnections with
 * remote devices. It also handle sending and receiving of network packets. The main purpose is too
 * manage connections and ensure accurate data about remote devices is delivered to application
 * processes. It implements thread safe code to ensure there are no race conditions when adding
 * or removing remote devices. */

object Async {

    /**Possible States*/
    const val IDLE = 0
    const val READY = 1
    const val INSYNC = 2

    val live_state = MutableLiveData(IDLE)
    private var state = IDLE
    //private val state_lock = Mutex()

    private var message_number = 0

    private val _peers = mutableListOf<User>()
    val peers = MutableLiveData<MutableList<User>>(mutableListOf()) //updates connected peers
    val peerStart = HashMap<String,Long>()

    /**All passed to SyncOperator, used to notify app UI of important packets*/
    val ping = MutableLiveData<String>()//updates pings
    val direct = MutableLiveData<String>()//updates on direct message

    private lateinit var ds : SyncDatabase
    private lateinit var op : SyncOperator
    private lateinit var vr : Verifier
    private lateinit var nl : Handler //NetworkLooper
    private lateinit var pn : PeerNetwork
    private lateinit var sw : MeshService.NetworkSwitch

    suspend fun me() : User{
        return ds.userDao.wait(PUBLIC_KEY.toBase64())!!
    }

    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

    suspend fun state() : Int{
      //  state_lock.lock()
        val ret = state
      //  state_lock.unlock()
        return ret
    }

    suspend fun handshake() : HandShake {
       // state_lock.lock()
        val shake = HandShake(state, me(), null,null)
       // state_lock.unlock()

        return shake
    }

    suspend fun ready(context : Context, nl : Handler, switch : MeshService.NetworkSwitch){
       // state_lock.lock()
//        if(state != IDLE){state_lock.unlock();return}

        pn = PeerNetwork()

        sw = switch
        this.nl = nl
        vr = Verifier(ping)

        ds = SyncDatabase.getInstance(context)
        val ca = ChannelAccess(ds.channelDao)
        val pr = PostRepository(ds.postDao)
        op = SyncOperator(pr, UserBase(ds.userDao), ca)

//        op.setPing(ping)
        op.setVerifier(vr)
        op.setMsg(direct)

        Sync.start(ca,pr)
//        Sync.queueUpdate()

        state = READY
        live_state.postValue(state)

     //   state_lock.unlock()
    }

    suspend fun checkKey(key: String): Boolean {
        //  state_lock.lock()
        //        Log.v(ASYNC_TAG,"$key : $ret")
        //  state_lock.unlock()
        return pn.contains(key)
    }


    suspend fun connect(socket : Socket, user : User) : Boolean{
        Log.d(ASYNC_TAG,"Calling connect on ${socket.key}")

      //  state_lock.lock()

        op.insertUser(user)

        val ret = pn.add(socket)

        if(ret && !_peers.contains(user)){
            val time = elapsedRealtime()
            peerStart[user.key] = time
            _peers.add(user)
            peers.postValue(_peers)
            Sync.add(user.key)
            Sync.queueUpdate()
        }

        state = pn.state()
        live_state.postValue(state)
        if(state == INSYNC){sw.off()}

        //Sync.queueUpdate()
        print_state()
      //  state_lock.unlock()
        return ret
    }

    suspend fun getSocket(device : BluetoothDevice) : Socket?{
      //  state_lock.lock()
        val ret = pn.getSocket(device)
     //   state_lock.unlock()
        return ret
    }

    suspend fun disconnectSocket(socket : Socket){
        Log.i(ASYNC_TAG,"disconnectSocket() called")
        vr.clear(socket)

      //  state_lock.lock()

        if(socket.type == Socket.BLUETOOTH_GATT){
            nl.obtainMessage(APP,
                AppEvent(socket)
            ).sendToTarget()
        }

        if(pn.closeSocket(socket) == 0){
            _peers.remove(socket.user)
            peers.postValue(_peers)

            if(state == INSYNC){
                state = pn.state()
                live_state.postValue(state)
                sw.on()
            }
        }

        print_state()
      //  state_lock.unlock()
    }

    suspend fun disconnect(user : User){
        val key = user.key
        val raw = MeshRaw(MeshRaw.DISCONNECT, null, null, null, null, null)
        send(raw,key)

        pn.get(key,Socket.BLUETOOTH_DEVICE)?.let{
            vr.clear(it)
        }

        pn.get(key,Socket.BLUETOOTH_GATT)?.let{
            vr.clear(it)
        }

       // state_lock.lock()
        val gatt_socket = pn.gattConnection(key)
        gatt_socket?.let{
            nl.obtainMessage(APP,
                AppEvent(it)
            ).sendToTarget()
        }

        val dis_cmnt = Comment(key,"d1sc0nn3ct",0L)
        val json = Json.encodeToString(dis_cmnt)
        direct.postValue(json)

        pn.removePeer(key)
        _peers.remove(user)
        peers.postValue(_peers)
        Sync.remove(key)

        if(state == INSYNC){
            state = pn.state()
            live_state.postValue(state)
            sw.on()
        }
      //  state_lock.unlock()

    }

    suspend fun canSend(key : String) : Boolean{
        return pn.gattConnection(key) != null
    }

    suspend fun send(raw : MeshRaw, key : String){
        pn.gattConnection(key)?.let { send(raw, it) }
    }

    suspend fun send(raw : MeshRaw, socket : Socket){
       // state_lock.lock()
//        if(state == IDLE){state_lock.unlock();return}

        if(raw.type != MeshRaw.CONFIRM) {
            raw.mid = message_number++
            val detail = Json.encodeToString(raw)
            Log.v(ASYNC_TAG, "Sending ${type2string(raw.type)} mid ${raw.mid} \n $detail")
        }else{
            Log.v(ASYNC_TAG, "Sending ${type2string(raw.type)} for mid ${raw.misc!!}")
        }

        val packer = Packer(raw)

        val success = socket.send(packer)

        //state_lock.unlock()

        if(success != 0){
            disconnectSocket(socket);return
        }

        val cur = System.currentTimeMillis()
        if(raw.type != MeshRaw.CONFIRM){
            vr.add(socket, Triple(raw.mid,packer.size,cur))
        }

    }

    suspend fun sendAll(raw : MeshRaw){
        for (p in pn.peers()){
            pn.getAny(p)?.let{send(raw,it)}
        }
    }

    suspend fun receive(bytes : ByteArray, socket : Socket){
        if(state == IDLE){return}
        op.receive(bytes,socket)
    }

    fun zeroAll(){
        for (p in pn.peers()){
            val dis_cmnt = Comment(p,"d1sc0nn3ct",0L)
            val json = Json.encodeToString(dis_cmnt)
            direct.postValue(json)
        }
    }

    suspend fun idle(){
        val raw = MeshRaw(MeshRaw.DISCONNECT, null, null, null, null, null)
        sendAll(raw)

        zeroAll()
      //  state_lock.lock()
//        if(state == IDLE){state_lock.unlock();return}
        Sync.kill()

        pn.clear()

        _peers.clear()
        peers.postValue(_peers)
        state = IDLE
        live_state.postValue(state)

     //   state_lock.unlock()
    }

    private fun print_state() {
        pn.print_state()
    }

    fun state2String() : String{
        return when(state){
            IDLE ->{"IDLE"}
            READY->{"READY"}
            INSYNC->{"INSYNC"}
            else->{"ERROR STATE NO DEFINED"}
        }
    }

    private fun type2string(type : Int) : String{

       return when(type){
            MeshRaw.INFO ->{"INFO"}
            MeshRaw.POST_LIST->{"POST_LIST"}
            MeshRaw.POST_W_ATTACH->{"POST_W_ATTACH"}
            MeshRaw.REQUEST -> {"REQUEST"}
            MeshRaw.NEW_DATA -> {"NEW_DATA"}
            MeshRaw.PING->{"PING"}
            MeshRaw.DISCONNECT->{"DISCONNECT"}
            MeshRaw.CONFIRM->{"CONFIRM"}
           MeshRaw.DIRECT->{"DIRECT"}

           else ->{"NO_TYPE"}
        }

    }
}