package daemon.dev.field.network.handler

import android.content.Context
import android.util.Log
import daemon.dev.field.OPERATOR_TAG
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.data.ChannelAccess
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase
import daemon.dev.field.data.db.SyncDatabase
import daemon.dev.field.network.Socket
import daemon.dev.field.network.util.Sorter
import daemon.dev.field.network.util.Verifier


/**@brief this class implements the handling of network packets. The packets are constructed from
 * segments by the Sorter class. The raw data is then handled by message type via the switch statement.*/

class SyncOperator(val context : Context) {
    private var postRepository: PostRepository
    private var userBase : UserBase
    private var channelAccess : ChannelAccess

    init {
        val sync = SyncDatabase.getInstance(context)
        val postDao = sync.postDao
        val userDao = sync.userDao
        val channelDao = sync.channelDao
        postRepository= PostRepository(postDao)
        userBase= UserBase(userDao)
        channelAccess= ChannelAccess(channelDao)
    }

    private val sorter = Sorter()
//    private val infoHandler = INFOHandler(userBase,channelAccess,postRepository)
    private val newDataHandler = NEW_DATAHandler(postRepository,channelAccess)
    private val postListHandler = POST_LISTHandler(postRepository,channelAccess)
    private val channelHandler = CHANNELHandler(channelAccess)

    fun receive(bytes : ByteArray, socket : Socket) : MeshRaw?{

        val plain = socket.decrypt(bytes)
        val msg : MeshRaw? = sorter.resolve(plain)

        return msg?.let{
            dataToReceive(it,socket)
        }

    }

    private fun dataToReceive(raw : MeshRaw, socket : Socket) : MeshRaw? {

        Log.i(OPERATOR_TAG,"Received ${type2String(raw.type)} , mid: ${raw.misc.toString()} from peer[${socket.key}]")

        return when(raw.type){
            MeshRaw.POST_LIST->{
                postListHandler.handle(raw)
                null
            }
            MeshRaw.NEW_DATA -> {
                newDataHandler.handle(raw,socket)
                null
            }
            MeshRaw.CHANNEL->{
                channelHandler.handle(raw.misc!!,socket.key)
                null
            }
            MeshRaw.INFO ->{ raw }
            MeshRaw.PING->{ raw }
            MeshRaw.DIRECT->{ raw }
            MeshRaw.DISCONNECT->{ raw }
            else -> {null}
        }

    }
    private fun type2String(type : Int) : String {
        return when (type) {
            MeshRaw.INFO -> { "INFO" }
            MeshRaw.POST_LIST -> { "POST_LIST" }
            MeshRaw.POST_W_ATTACH -> { "POST_W_ATTACH" }
            MeshRaw.REQUEST -> { "REQUEST" }
            MeshRaw.NEW_DATA -> { "NEW_DATA" }
            MeshRaw.PING -> { "PING" }
            MeshRaw.CHANNEL -> { "CHANNEL" }
            MeshRaw.DIRECT -> { "DIRECT" }
            MeshRaw.DISCONNECT -> { "DISCONNECT" }
            MeshRaw.CONFIRM -> { "CONFIRM" }
            else -> { "NO_TYPE" }
        }
    }
}