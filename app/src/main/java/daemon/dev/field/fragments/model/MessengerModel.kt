package daemon.dev.field.fragments.model

import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.PUBLIC_KEY
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.cereal.objects.User
import daemon.dev.field.network.Async
import daemon.dev.field.network.Sync
import daemon.dev.field.network.util.LoadBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.collections.ArrayDeque

class MessengerModel : ViewModel() {

    val direct = Async.direct

    private val sessMap = hashMapOf<String, AMSession>()
    private val waiting_queue = hashMapOf<String, ArrayDeque<String>>()

    val latest = MutableLiveData<Comment>()

    val loadControllers = mutableListOf<LoadBox>() //for test purposes


    fun init_loadBox(key : String){
        val box = LoadBox(key,1000)
        loadControllers.add(box)
        box.start()
    }

    fun killLoad(){
        for (b in loadControllers){
            b.kill()
        }
    }

    fun load(key : String) : Boolean {
        for (b in loadControllers){
            if(b.key.equals(key)){
                return true
            }
        }
        return false
    }

    fun setLatest(cmnt : Comment){
        latest.postValue(cmnt)
    }

    fun printMsgMap(){
        for ((i, c) in sessMap){
            Log.v("MessengerModel","User[$i] ${sessMap[i]?.nonViewSub()}")
            c.print()
        }
    }

    fun zeroSub(key : String){
        sessMap[key]?.let{
            it.zeroSub()
        }
    }

    fun getSub(key : String) : List<Comment>?{
        return sessMap[key]?.let{
            it.sub()
        }
    }

    fun getUnRead(key : String) : Int?{
        return sessMap[key]?.unRead()
    }

    fun receiveMessage(msg : Comment){
        sessMap[msg.key]?.addInOrder(msg)
    }

    fun send(mesg : String, key : String){

        val delta = SystemClock.elapsedRealtime() - Async.peerStart[key]!!
        val msg = Comment(PUBLIC_KEY.toBase64(),mesg,delta)
        val json = Json.encodeToString(msg)

        val raw = MeshRaw(
            MeshRaw.DIRECT,
            null,
            null,
            null,
            null,
            json
        )

        viewModelScope.launch(Dispatchers.IO) {

            Async.peers.value?.let{ peers ->
                val connected = peers.contains(User(key, "", 0, ""))
                if (connected) {
                    Sync.queue(key, raw)
                    sessMap[key]?.addInOrder(msg)
                    latest.postValue(msg)
                } else {
                    waitQueue(key,mesg)
                }
            }
        }

    }

    private fun waitQueue(key : String, cmnt : String){
        sessMap[key]?.let{
            if(waiting_queue[key]==null){
                waiting_queue[key] = ArrayDeque()
            }

            waiting_queue[key]!!.add(cmnt)
        }
    }

    fun dumpQueue(key : String){
        sessMap[key]?.let{

            val queue = waiting_queue[key]

            queue?.let{
                while(queue.isNotEmpty()){
                    send(queue.removeFirst(),key)
                }
            }

        }
    }

    fun createSub(key : String){

        if(sessMap[key]==null) {
            val session = AMSession(key)
            sessMap[key] = session
        }

    }


    private fun ByteArray.toBase64() : String {
        return Base64.getEncoder().encodeToString(this)
    }

}