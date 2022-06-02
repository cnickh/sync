package daemon.dev.field.data

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.annotation.RequiresApi
import daemon.dev.field.KILL_LOOPER
import daemon.dev.field.RESOLVER_LOOPER
import daemon.dev.field.SERVER_LOOPER
import daemon.dev.field.data.PostRAM.getFromDB
import daemon.dev.field.data.objects.MeshRaw
import daemon.dev.field.network.PeerRAM
import daemon.dev.field.network.Sorter
import daemon.dev.field.util.Serializer
import kotlinx.coroutines.runBlocking

@RequiresApi(Build.VERSION_CODES.O)
class Resolver(val server : Handler) : Thread(), Handler.Callback {

    var mHandler: Handler? = null
    var mLooper: Looper? = null
    val serializer = Serializer()
    private val sorter = Sorter()

    fun getHandler() : Handler {
        Log.d(RESOLVER_LOOPER,"Get handler called")
        while(mHandler==null){Log.d(RESOLVER_LOOPER,"Hanging loose")}
        return mHandler!!
    }

    override fun run() {
        Log.i(RESOLVER_LOOPER, "Starting...")
        Looper.prepare()
//        Log.d(RESOLVER_LOOPER,"looper prepared")
        mLooper = Looper.myLooper()
//        Log.d(RESOLVER_LOOPER,"Got looper")
        mHandler = Handler(mLooper!!,this)
//        Log.d(RESOLVER_LOOPER,"Handler set :)")
        Looper.loop()
        Log.i(RESOLVER_LOOPER, "Killed Successfully")
    }

    override fun handleMessage(p0: Message): Boolean {
        Log.i(RESOLVER_LOOPER, "Handler requested")

        val sid = p0.what
        val bytes = p0.obj as ByteArray

        val msg = sorter.resolve(bytes)

        if(msg != null){
            Log.i(RESOLVER_LOOPER, "Packet successfully constructed")
            dataToReceive(sid,msg)
        }

        return false
    }

    private fun dataToReceive(sid : Int, packet: MeshRaw){

        val mtype : String

        when(packet.type){
            MeshRaw.INFO ->{
                mtype = "INFO"
                val info = packet.nodeInfo
                val uid = PeerRAM.getUserAtSock(sid)
                if(uid != null){
                    info?.let { PeerRAM.putNodeInfo(it) }
                }
                //TODO request shared bin posts
            }
            MeshRaw.POST_LIST->{
                mtype = "POST_LIST"
                packet.posts?.forEach {
                    Log.v(RESOLVER_LOOPER,"Received post : ${it.uid} : ${serializer.postHash(it)}")
                    PostRAM.storeNetworkPost(it)
                }
            }
            MeshRaw.POST_W_ATTACH->{
                mtype = "POST_W_ATTACH"
            }
            MeshRaw.REQUEST -> {
                mtype = "REQUEST"
                server.obtainMessage(0,
                    Server.Request(sid, Server.SPECIFIC, packet.requests, null)).sendToTarget();
            }
            MeshRaw.NEW_DATA -> {
                mtype = "NEW_DATA"
                packet.newData?.let {
                    Log.v(RESOLVER_LOOPER,"Comparing new data")
                    comparePublicPosts(it)?.let{ posts ->
                        Log.v(RESOLVER_LOOPER,"Sending request packet")
                        server.obtainMessage(0,
                            Server.Request(sid, Server.REQUEST, posts, null)).sendToTarget();
                    }
                }
            }
            MeshRaw.PING->{
                mtype = "PING"
            }
            MeshRaw.DISCONNECT->{
                mtype = "DISCONNECT"
                runBlocking {  PeerRAM.disconnectPeer(sid) }
            }
            else ->{
                mtype = "NO_TYPE"
            }
        }

        Log.i(RESOLVER_LOOPER,"Received $mtype from peer[$sid]")
    }

    private fun comparePublicPosts(postsMap : HashMap<ULong,String>) : List<ULong>?{

        val postsToRequest = mutableListOf<ULong>()
        var dif = false


        for (key in postsMap.keys) {

            val post = getFromDB(key.toLong())

            if(post != null){
                Log.d(RESOLVER_LOOPER,
                    "Comparing post : ${post.uid} : ${postsMap[key]} to ${serializer.postHash(post)}")
                if(serializer.postHash(post).toString()!=postsMap[key]){
                    dif = true
                    postsToRequest.add(key)
                }
            } else {
                dif = true
                postsToRequest.add(key)
            }

        }

        return if(!dif){
            null
        }else {
            Log.d(RESOLVER_LOOPER,"Difference found making request")
            postsToRequest
        }

    }

    fun kill(){
        mLooper?.quit()
    }

}