package daemon.dev.field.data

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.BLE_INTERVAL
import daemon.dev.field.KILL_LOOPER
import daemon.dev.field.RESOLVER_LOOPER
import daemon.dev.field.SERVER_LOOPER
import daemon.dev.field.data.objects.NodeInfo
import daemon.dev.field.data.objects.Post
import daemon.dev.field.data.objects.MeshRaw

import daemon.dev.field.util.Serializer
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.network.Packer
import kotlinx.coroutines.runBlocking

/**Network Controller
 *
 * This class implements a node that acts as a state machine
 * responding to requests from the network. It is aptly named
 * samaritan for its service to other nodes and application processes.
 *
 * */
@RequiresApi(Build.VERSION_CODES.O)
class Server : Thread(), Handler.Callback {

    companion object{

        /**Request Action*/
        const val INFO = 2
        const val BIN = 3
        const val REQUEST = 4
        const val SPECIFIC = 5
        const val NOTIFY = 6
        const val PING = 7
        const val DISCONNECT = 8

    }

    var mHandler: Handler? = null
    var mLooper : Looper? = null
    val serializer = Serializer()

    class Request(val sid: Int, val action: Int, val posts: List<ULong>?, val bin: String?)

    fun getHandler() : Handler {
        Log.d(SERVER_LOOPER,"Get handler called")
        while(mHandler==null){  Log.d(SERVER_LOOPER,"Hanging loose") }
        return mHandler!!
    }

    override fun run() {
        Log.i(SERVER_LOOPER, "Starting...")
        Looper.prepare()
//        Log.i(SERVER_LOOPER, "prepared")
        mLooper = Looper.myLooper()
//        Log.i(SERVER_LOOPER, "Have looper")
        mHandler = Handler(mLooper!!,this)
//        Log.i(SERVER_LOOPER, "Handler set :)")
        Looper.loop()
        Log.i(SERVER_LOOPER, "Killed Successfully")
    }

    override fun handleMessage(msg: Message): Boolean {

        Log.d(SERVER_LOOPER,"Handler requested")

        runBlocking {
            PeerRAM.cLock.lock()
            dataToSend(msg.obj as Request)
            PeerRAM.cLock.unlock()
        }
        return false
    }

    private fun dataToSend(request : Request){

        val sid = request.sid
        val peer = PeerRAM.getConnectionBySid(sid)
        val raw : MeshRaw

        val mRequest : String

        when(request.action){
            INFO -> {
                mRequest = "INFO"
                raw = MeshRaw(MeshRaw.INFO,getInfo(),null,null,null,null)
            }
            BIN -> {
                mRequest = "BIN"
                raw = MeshRaw(MeshRaw.POST_LIST,null,null,null,getBinPosts(request.bin!!),null)
            }
            REQUEST -> {
                mRequest = "REQUEST"
                raw = MeshRaw(MeshRaw.REQUEST,null,request.posts,null,null,null)
            }
            SPECIFIC -> {
                mRequest = "SPECIFIC"
                raw = MeshRaw(MeshRaw.POST_LIST,null,null,null,getSpecific(request.posts!!),null)
            }
            NOTIFY ->{
                mRequest = "NOTIFY"
                raw = MeshRaw(MeshRaw.NEW_DATA,null,null,hashNewPosts(request.posts!!),null,null)
            }
            DISCONNECT -> {
                mRequest = "DISCONNECT"
                raw = MeshRaw(MeshRaw.DISCONNECT,null,null,null,null,null)
            }
            else -> {
                mRequest = "PING"
                raw = MeshRaw(MeshRaw.PING,null,null,null,null,null)
            }

        }
        Log.d(SERVER_LOOPER,"Sending $mRequest to peer[$sid]...")

        raw.posts?.let{
            if(it.isNotEmpty()){
                val  comments = serializer.commentListToString(it[0].comments)
                Log.v(SERVER_LOOPER,"Checking comments : $comments")
                Log.v(SERVER_LOOPER, "Post Details : ${it[0].uid} : ${serializer.postHash(it[0])}")

            }
        }

        val test = serializer.packetToByte(raw)
        val clean = serializer.getPacket(test)
        clean?.posts?.let{
            if(it.isNotEmpty()){
                val  comments = serializer.commentListToString(it[0].comments)
                Log.d(SERVER_LOOPER,"Sending post with comments : $comments")
                Log.d(SERVER_LOOPER, "Post Details : ${it[0].uid} : ${serializer.postHash(it[0])}")

            }
        }

        val packer = Packer(raw)
        var buffer = packer.next()

        while(buffer != null){

            peer.write(buffer)
            buffer = packer.next()

            if(!PeerRAM.res.block(BLE_INTERVAL)){
                Log.d(SERVER_LOOPER,"Failed to send $mRequest to peer[$sid]")
            } else {
                Log.d(SERVER_LOOPER,"Sent $mRequest to peer[$sid]")
            }

        }

        PeerRAM.res.close()

    }

    private fun getBinPosts(bin : String) : List<Post>?{
        return listOf()
    }

    private fun getSpecific(posts : List<ULong>) : List<Post>{

        val specific = mutableListOf<Post>()

        posts.forEach {
            PostRAM.getFromDB(it.toLong())?.let{ post ->
                specific.add(post)

                Log.v(SERVER_LOOPER, "Packing post : ${post.uid} : ${serializer.postHash(post)}")
                Log.v(SERVER_LOOPER, "Getting post with : ${serializer.commentListToString(post.comments)}")

            }
        }

        return specific
    }


    private fun getInfo() : NodeInfo {
        return PostRAM.buildDeviceInfo()
    }

    private fun hashNewPosts(posts : List<ULong>) : HashMap<ULong,String>{

        val postsHash = hashMapOf<ULong,String>()

        posts.forEach {
            PostRAM.getFromDB(it.toLong())?.let{ post ->
                postsHash[it] = serializer.postHash(post).toString()
                Log.d(SERVER_LOOPER,
                    "Sending post : ${post.uid} : ${postsHash[it]}")
                Log.d(SERVER_LOOPER,
                "With comments ${serializer.commentListToString(post.comments)}")
            }

        }

        return postsHash

    }

    fun kill(){
        mLooper?.quit()
    }


}