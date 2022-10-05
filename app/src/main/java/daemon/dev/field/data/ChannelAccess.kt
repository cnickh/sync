package daemon.dev.field.data

import androidx.lifecycle.LiveData
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.ChannelDao
import daemon.dev.field.network.Sync

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()

    suspend fun getOpenContents() : List<String>{
        val posts = mutableListOf<String>()

        for(c in Sync.open_channels){
            val content = sync.waitContents(c).split(",")
            for(p in content) {
                if(!posts.contains(p)) posts.add(p)
            }
        }

        return posts
    }

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

    suspend fun resolveChannels(channels : List<String>) : List<String>{
        val posts = mutableListOf<String>()

        for(c in channels){
            if(Sync.open_channels.contains(c)){
                val content = sync.waitContents(c).split(",")
                for(p in content) {
                    if(!posts.contains(p)) posts.add(p)
                }
            }
        }

        return posts
    }


}