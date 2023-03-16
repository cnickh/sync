package daemon.dev.field.fragments.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import daemon.dev.field.cereal.objects.Comment
import daemon.dev.field.cereal.objects.MeshRaw
import daemon.dev.field.network.Async
import daemon.dev.field.network.Sync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MessengerModel : ViewModel() {

    val direct = Async.direct

    val msgMap = mutableListOf<Comment>()

    fun printMsgMap(){
        for ((i, c) in msgMap.withIndex()){
            //val json = Json.encodeToString(c)
            Log.v("MessengerModel","User[$i]")

            for (c in c.sub){
                Log.v("MessengerModel","${c.time}  ${c.comment}")
            }

        }
    }

    fun zeroSub(key : String){
        getKey(key)?.let{

            for (c in it.sub){
                c.time = 0L
            }

        }
    }

    fun send(msg : Comment, key : String){

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
            Sync.queue(key, raw)
        }

        addMyMessage(msg,key)
    }

    fun createSub(key : String){

        var root = getKey(key)

        if(root == null){
            root = Comment(key,"root",0L)
            msgMap.add(root)
        }

    }

    fun receiveMessage(msg : Comment) : Boolean{
        val key = msg.key

        val root = getKey(key)

        if(root != null){
            addInOrder(msg,root.sub)
            return true
        }

        return false
    }

    private fun addMyMessage(msg : Comment, key : String) : Boolean {

        val root = getKey(key)

        if(root != null){
            addInOrder(msg,root.sub)
            return true
        }

        return false
    }

    fun getKey(key : String) : Comment?{
        for (m in msgMap){
            if(m.key == key){
                return m
            }
        }

        return null
    }

    private fun addInOrder(msg : Comment, sub : MutableList<Comment>){

        val time = msg.time

        for ((prev, m) in sub.withIndex()){
            if(time < m.time){
                sub.add(prev,msg)
                return
            }
        }

        sub.add(msg)
    }


}