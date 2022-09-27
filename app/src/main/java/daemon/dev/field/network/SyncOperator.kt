package daemon.dev.field.network

import android.util.Log
import androidx.lifecycle.MutableLiveData
import daemon.dev.field.CHARSET
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.data.PostRepository
import daemon.dev.field.data.UserBase


/**@brief this class implements the handling of network packets. The packets are constructed from
 * segments by the Sorter class. The raw data is then handled by message type via the switch statement.*/

class SyncOperator(private val postRepository: PostRepository, private val userBase: UserBase) {

    lateinit var new_thread : MutableLiveData<String>
    lateinit var livePing : MutableLiveData<String>

    lateinit var vr : Verifier

    private val sorter = Sorter()

    fun setVerifier(verifier: Verifier){
        vr = verifier
    }

    fun setLiveData(thread : MutableLiveData<String>){
        new_thread = thread
    }

    fun setPing(ping : MutableLiveData<String>){
        livePing = ping
    }

    suspend fun insertUser(user : User){
        userBase.add(user)
        Log.i("op.kt"," got user: ${userBase.wait(user.key)}")
    }

    suspend fun receive(bytes : ByteArray, socket : Socket){
        val msg = sorter.resolve(bytes)
        if(msg != null){
            Log.e("Op.kt", "Packet construction success -\n $msg")

            dataToReceive(msg,socket)
        }else{
            Log.e("Op.kt", "Packet construction failed")
        }
    }


    private suspend fun dataToReceive(raw : MeshRaw, socket : Socket){

        val mtype : String

        when(raw.type){
            MeshRaw.INFO ->{
                mtype = "INFO"
                userBase.update(raw.nodeInfo!!)
            }

            MeshRaw.POST_LIST->{
                mtype = "POST_LIST"
                for(p in raw.posts!!){
                    p.index=0
                    p.hops++
                    postRepository.add(p)
                    val test = postRepository.getAt(p.address())
                    Log.d("Op.kt", "Test comments : ${test?.comment}")
                    Log.d("Op.kt", "Sending signal on new_thread")
                    new_thread.postValue(p.address().address)
                }
            }

            MeshRaw.POST_W_ATTACH->{
                mtype = "POST_W_ATTACH"
            }

            MeshRaw.REQUEST -> {
                mtype = "REQUEST"
                val list = postRepository.getList(raw.requests!!)
                val newRaw = MeshRaw(
                    MeshRaw.POST_LIST,
                    null,
                    null,
                    null,
                    list,
                    null
                )
//                Log.i("op.kt", "Requests: ${raw.requests}")
//                Log.i("op.kt", "Send post ${list[0]} with comments ${list[0].comment}")
                Async.send(newRaw,socket)
            }

            MeshRaw.NEW_DATA -> {
                mtype = "NEW_DATA"
                raw.newData?.let {
                    postRepository.compare(it).let { posts ->
                        val newRaw = MeshRaw(
                            MeshRaw.REQUEST,
                            null,
                            posts,
                            null,
                            null,
                            null
                        )
                        if(posts.isNotEmpty()){
                            Async.send(newRaw,socket)
                        }
                    }
                }
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
                vr.confirm(raw.misc!!.toString(CHARSET))
            }
            else ->{
                mtype = "NO_TYPE"
            }

        }

        Log.i("Op.kt","Received $mtype from peer[${socket.key}]")
        if(raw.type != MeshRaw.CONFIRM){

            val sig_bytes = raw.hash().toByteArray(CHARSET)
            val nw_raw = MeshRaw(MeshRaw.CONFIRM,null,null,null,null,sig_bytes)

            Async.send(nw_raw,socket)
        }


    }

}