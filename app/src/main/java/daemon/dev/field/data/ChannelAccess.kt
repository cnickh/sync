package daemon.dev.field.data

import androidx.lifecycle.LiveData
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.ChannelDao

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()

    fun contents(name : String) : LiveData<String>{
        return sync.getContents(name)
    }

    suspend fun createChannel(name : String, key : String){
        val ch = Channel(name, key,"null")
        sync.insert(ch)
    }

    suspend fun addPost(name : String, post : Post){
        var content = sync.waitContents(name)

        content = if(content == "null"){
            post.address().address
        }else{
            val cnt = content.split(",") as MutableList<String>
            cnt.add(post.address().address)
            cnt.joinToString(",")
        }

        sync.updateContents(name,content)

    }

    suspend fun key(name : String) : String {
        return sync.getKey(name)
    }



}