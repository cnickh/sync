//package daemon.dev.field.data
//
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import android.os.Message
//import android.util.Log
//import androidx.annotation.RequiresApi
//import daemon.dev.field.RESOLVER_LOOPER
//import daemon.dev.field.cereal.objects.MeshRaw
//import daemon.dev.field.network.Sorter
//import kotlinx.coroutines.runBlocking
//
//@RequiresApi(Build.VERSION_CODES.O)
//class Resolver(val server : Handler) : Thread(), Handler.Callback {
//
//    var mHandler: Handler? = null
//    var mLooper: Looper? = null
//    val postRepository = PeerRAM.postRepository!!
//    val userBase = PeerRAM.userBase!!
//    private val sorter = Sorter()
//
//    fun getHandler() : Handler {
//        Log.d(RESOLVER_LOOPER,"Get handler called")
//        while(mHandler==null){Log.d(RESOLVER_LOOPER,"Hanging loose")}
//        return mHandler!!
//    }
//
//    override fun run() {
//        Log.i(RESOLVER_LOOPER, "Starting...")
//        Looper.prepare()
////        Log.d(RESOLVER_LOOPER,"looper prepared")
//        mLooper = Looper.myLooper()
////        Log.d(RESOLVER_LOOPER,"Got looper")
//        mHandler = Handler(mLooper!!,this)
////        Log.d(RESOLVER_LOOPER,"Handler set :)")
//        Looper.loop()
//        Log.i(RESOLVER_LOOPER, "Killed Successfully")
//    }
//
//    override fun handleMessage(p0: Message): Boolean {
//        Log.i(RESOLVER_LOOPER, "Handler requested")
//
//        val sid = p0.what
//        val bytes = p0.obj as ByteArray
//
//        val msg = sorter.resolve(bytes)
//
//        Log.i(RESOLVER_LOOPER, "Have array of size: ${bytes.size}")
//
//
//        if(msg != null){
//            Log.i(RESOLVER_LOOPER, "Packet successfully constructed")
//            dataToReceive(sid,msg)
//        }else{
//            Log.e(RESOLVER_LOOPER, "Packet construction failed")
//        }
//
//        return false
//    }
//
//    private fun dataToReceive(sid : Int, packet: MeshRaw){
//
//        val mtype : String
//
//        when(packet.type){
//            MeshRaw.INFO ->{
//                mtype = "INFO"
//                runBlocking{ userBase.update(packet.nodeInfo!!) }
//            }
//
//            MeshRaw.POST_LIST->{
//                mtype = "POST_LIST"
//                runBlocking {
//                    postRepository.stage(packet.posts!!)
//                    postRepository.commit()
//                }
//            }
//
//            MeshRaw.POST_W_ATTACH->{
//                mtype = "POST_W_ATTACH"
//            }
//
//            MeshRaw.REQUEST -> {
//                mtype = "REQUEST"
//                server.obtainMessage(0,
//                    Server.Request(sid, Server.SPECIFIC, packet.requests, null)).sendToTarget();
//            }
//
//            MeshRaw.NEW_DATA -> {
//                mtype = "NEW_DATA"
//                packet.newData?.let {
//                    runBlocking {
//                        postRepository.compare(it).let { posts ->
//                            server.obtainMessage(
//                                0,
//                                Server.Request(sid, Server.REQUEST, posts, null)
//                            ).sendToTarget();
//                        }
//                    }
//                }
//            }
//
//            MeshRaw.PING->{
//                mtype = "PING"
//            }
//
//            MeshRaw.DISCONNECT->{
//                mtype = "DISCONNECT"
//                runBlocking {  PeerRAM.disconnectPeer(sid) }
//            }
//            else ->{
//                mtype = "NO_TYPE"
//            }
//        }
//
//        Log.i(RESOLVER_LOOPER,"Received $mtype from peer[$sid]")
//
//    }
//
//
//
//    fun kill(){
//        mLooper?.quit()
//    }
//
//}