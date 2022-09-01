//package daemon.dev.field.data
//
//import android.os.Build
//import android.os.Handler
//import android.os.Looper
//import android.os.Message
//import android.util.Log
//import androidx.annotation.RequiresApi
//import daemon.dev.field.BLE_INTERVAL
//import daemon.dev.field.SERVER_LOOPER
//import daemon.dev.field.cereal.objects.Address
//import daemon.dev.field.cereal.objects.MeshRaw
//import daemon.dev.field.cereal.objects.Post
//
////import daemon.dev.field.cereal.Cerealizer
//import daemon.dev.field.network.PeerRAM
//import daemon.dev.field.network.Packer
//import kotlinx.coroutines.runBlocking
//
///**Network Controller
// *
// * This class implements a node that acts as a state machine
// * responding to requests from the network. It is aptly named
// * samaritan for its service to application processes. All
// * calls are received solely from other application threads
// *
// * */
//@RequiresApi(Build.VERSION_CODES.O)
//class Server : Thread(), Handler.Callback {
//
//    companion object{
//
//        /**Request Action*/
//        const val INFO = 2
//        const val BIN = 3
//        const val REQUEST = 4
//        const val SPECIFIC = 5
//        const val NOTIFY = 6
//        const val PING = 7
//        const val DISCONNECT = 8
//        const val RECENT = 9
//
//    }
//
//    var mHandler: Handler? = null
//    var mLooper : Looper? = null
//    val postRepository = PeerRAM.postRepository!!
//
//    class Request(val sid: Int, val action: Int, val posts: List<Address>?, val bin: String?)
//
//    fun getHandler() : Handler {
//        Log.d(SERVER_LOOPER,"Get handler called")
//        while(mHandler==null){  Log.d(SERVER_LOOPER,"Hanging loose") }
//        return mHandler!!
//    }
//
//    override fun run() {
//        Log.i(SERVER_LOOPER, "Starting...")
//        Looper.prepare()
////        Log.i(SERVER_LOOPER, "prepared")
//        mLooper = Looper.myLooper()
////        Log.i(SERVER_LOOPER, "Have looper")
//        mHandler = Handler(mLooper!!,this)
////        Log.i(SERVER_LOOPER, "Handler set :)")
//        Looper.loop()
//        Log.i(SERVER_LOOPER, "Killed Successfully")
//    }
//
//    override fun handleMessage(msg: Message): Boolean {
//
//        Log.d(SERVER_LOOPER,"Handler requested")
//
//        runBlocking {
//            PeerRAM.cLock.lock()
//            dataToSend(msg.obj as Request)
//            PeerRAM.cLock.unlock()
//        }
//        return false
//    }
//
//    private fun dataToSend(request : Request){
//
//        val sid = request.sid
//        val peer = PeerRAM.getConnectionBySid(sid)
//        val raw : MeshRaw
//
//        val mRequest : String
//
//        //Simple network state machine baby
//
//        when(request.action){
//            INFO -> {
//                mRequest = "INFO"
//                raw = MeshRaw(MeshRaw.INFO,null,null,null,null,null)
//            }
//            BIN -> {
//                mRequest = "BIN"
//                raw = MeshRaw(MeshRaw.POST_LIST,null,null,null,listOf(),null)
//            }
//            REQUEST -> {
//                mRequest = "REQUEST"
//                raw = MeshRaw(MeshRaw.REQUEST,null,request.posts,null,null,null)
//            }
//            SPECIFIC -> {
//                mRequest = "SPECIFIC"
//                runBlocking {
//                    raw = MeshRaw(
//                        MeshRaw.POST_LIST,
//                        null,
//                        null,
//                        null,
//                        postRepository.getList(request.posts!!),
//                        null
//                    )
//                }
//            }
//            NOTIFY -> {
//                mRequest = "NOTIFY"
//                val list = runBlocking {
//                    postRepository.hashList(request.posts!!)
//                }
//
//                raw = if(list == null){
//                    MeshRaw(MeshRaw.PING,null,null,null,null,null)
//                }else{
//                    MeshRaw(
//                        MeshRaw.NEW_DATA,
//                        null,
//                        null,
//                        list,
//                        null,
//                        null
//                    )
//                }
//            }
//            DISCONNECT -> {
//                mRequest = "DISCONNECT"
//                raw = MeshRaw(MeshRaw.DISCONNECT,null,null,null,null,null)
//            }
//            RECENT ->{
//                mRequest = "RECENT"
//                val list = runBlocking {
//                    postRepository.hashList()
//                }
//
//                raw = if(list == null){
//                    MeshRaw(MeshRaw.PING,null,null,null,null,null)
//                }else{
//                    MeshRaw(
//                        MeshRaw.NEW_DATA,
//                        null,
//                        null,
//                        list,
//                        null,
//                        null
//                    )
//                }
//
//            }
//            else -> {
//                mRequest = "PING"
//                raw = MeshRaw(MeshRaw.PING,null,null,null,null,null)
//            }
//
//        }
//        Log.d(SERVER_LOOPER,"Sending $mRequest to peer[$sid]...")
//
//        val packer = Packer(raw)
//        var buffer = packer.next()
//
//        while(buffer != null){
//
//            peer.write(buffer)
//            buffer = packer.next()
//
//            if(!PeerRAM.res.block(BLE_INTERVAL)){
//                Log.d(SERVER_LOOPER,"Failed to send $mRequest to peer[$sid]")
//            } else {
//                Log.d(SERVER_LOOPER,"Sent $mRequest to peer[$sid]")
//            }
//
//        }
//
//        PeerRAM.res.close()
//
//    }
//
//
//
//    fun kill(){
//        mLooper?.quit()
//    }
//
//
//}