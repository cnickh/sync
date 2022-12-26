package daemon.dev.field.network.util

import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.network.Async
import daemon.dev.field.network.Socket
import daemon.dev.field.network.handler.packet.INFOHandler
import daemon.dev.field.network.handler.packet.NEW_DATAHandler
import daemon.dev.field.network.handler.packet.POST_LISTHandler


/**@brief this class implements the handling of network packets. The packets are constructed from
 * segments by the Sorter class. The raw data is then handled by message type via the switch statement.*/

class SyncOperator(private val postRepository: PostRepository, private val userBase: UserBase, private val channelAccess : ChannelAccess) {

    lateinit var livePing : MutableLiveData<String>
    lateinit var vr : Verifier

    private val sorter = Sorter()
    private val infoHandler = INFOHandler(userBase,channelAccess,postRepository)
    private val newDataHandler = NEW_DATAHandler(postRepository,channelAccess)
    private val postListHandler = POST_LISTHandler(postRepository,channelAccess)

    fun setVerifier(verifier: Verifier){
        vr = verifier
    }

    fun setPing(ping : MutableLiveData<String>){
        livePing = ping
    }

    suspend fun insertUser(user : User){
        userBase.add(user)
//        Log.i("op.kt"," got user: ${userBase.wait(user.key)}")
    }

    suspend fun receive(bytes : ByteArray, socket : Socket){
        val msg = sorter.resolve(bytes)
        if(msg != null){
//            Log.e("Op.kt", "Packet construction success -\n $msg")
            dataToReceive(msg,socket)
        }
    }


    private suspend fun dataToReceive(raw : MeshRaw, socket : Socket){

        val mtype : String

        if(raw.type != MeshRaw.CONFIRM){
            val sig_bytes = ByteArray(4)
            bytesToBuffer(sig_bytes, raw.mid)
            val newRaw = MeshRaw(MeshRaw.CONFIRM,null,null,null,null,sig_bytes)
            Async.send(newRaw, socket)
            //Sync.queue(socket.key,raw)
        }

        when(raw.type){
            MeshRaw.INFO ->{
                mtype = "INFO"
                infoHandler.handle(raw,socket)
            }

            MeshRaw.POST_LIST->{
                mtype = "POST_LIST"
                postListHandler.handle(raw)
            }

            MeshRaw.POST_W_ATTACH->{
                mtype = "POST_W_ATTACH"
            }

            MeshRaw.REQUEST -> {
                mtype = "REQUEST"
            }

            MeshRaw.NEW_DATA -> {
                mtype = "NEW_DATA"
                newDataHandler.handle(raw,socket)
            }

            MeshRaw.PING->{
                mtype = "PING"
                livePing.postValue(socket.key)
            }

            MeshRaw.DISCONNECT->{
                mtype = "DISCONNECT"
                Async.disconnect(socket.user)
            }
            MeshRaw.CONFIRM->{
                mtype = "CONFIRM"
                vr.confirm(socket, bytesFromBuffer(raw.misc!!))
            }
            else ->{
                mtype = "NO_TYPE"
            }

        }

//        Log.i("Op.kt","Received $mtype , mid: ${raw.mid} from peer[${socket.key}]")

    }
    private fun bytesToBuffer(buffer: ByteArray, data: Int) {
        for (i in 0..3) buffer[i] = (data shr (i * 8)).toByte()
    }
    fun bytesFromBuffer(buffer: ByteArray): Int {
        return (buffer[3].toInt() shl 24) or
                (buffer[2].toInt() and 0xff shl 16) or
                (buffer[1].toInt() and 0xff shl 8) or
                (buffer[0].toInt() and 0xff)
    }
}