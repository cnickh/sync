package daemon.dev.field.data

import android.util.Log
import androidx.lifecycle.LiveData
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.cereal.objects.Post
import daemon.dev.field.data.db.ChannelDao
import daemon.dev.field.network.Sync

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()

    suspend fun waitContents(name : String) : String {
        return sync.waitContents(name)
    }

    suspend fun waitChannels() : List<String> {
        return sync.waitChannels()
    }

    suspend fun getOpenContents() : List<String>{
        val posts = mutableListOf<String>()

        for(c in Sync.open_channels){

            val content = sync.waitContents(c)
//            Log.i("ChannelAccess","$c :: $content")

            if(content == null){return listOf()}

            if(content!="null") {
                for (p in content.split(",")) {
                    if (!posts.contains(p)) posts.add(p)
                }
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

    suspend fun addPost(name : String, address : String){
        var content = sync.waitContents(name)

        content = if(content == "null"){
            address
        }else{
            val cnt : MutableList<String> = mutableListOf()

            for(c in content.split(",")){
               cnt.add(c)
            }

//            Log.i("ChannelAccess","$cnt adding $address")

            if(!cnt.contains(address)){ cnt.add(address)}
            cnt.joinToString(",")
        }

        sync.updateContents(name,content)

    }

    suspend fun key(name : String) : String {
        return sync.getKey(name)
    }

    suspend fun resolveChannels(channels : List<String>) : HashMap<String,MutableList<String>>{

        val ch_map = hashMapOf<String,MutableList<String>>()

        for(c in channels){
            if(Sync.open_channels.contains(c)){
                val content = sync.waitContents(c).split(",")

                if(content[0] != "null") {
                    for (p in content) {

                        if(ch_map[c]==null){
                            ch_map[c] = mutableListOf(p)
                        }else{
                            if(!ch_map[c]!!.contains(p)) ch_map[c]!!.add(p)
                        }
                    }
                }

            }
        }

        return ch_map
    }


}