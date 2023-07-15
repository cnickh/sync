package daemon.dev.field.data
import daemon.dev.field.cereal.objects.Channel
import daemon.dev.field.data.db.ChannelDao
import daemon.dev.field.network.Sync

class ChannelAccess(private val sync : ChannelDao) {

    val channels = sync.getChannels()

    fun getLiveContents(list : List<String>) = sync.getLiveContents(list)

    fun clear(){
        sync.clear()
    }

    suspend fun waitContents(name : String) : String {
        return sync.waitContents(name)
    }

    suspend fun delete(name : String){
        sync.delete(name)
    }

    suspend fun updateKey(name : String, key : String){
        sync.updateKey(name,key)
    }

    suspend fun getOpenContents() : List<String>{
        val posts = mutableListOf<String>()

        for(c in Sync.getOpenChannels()){

            val content = sync.waitContents(c)

            if(content!="null") {
                for (p in content.split(",")) {
                    if (!posts.contains(p)) posts.add(p)
                }
            }

        }

        return posts
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

    suspend fun mapOpenChannels(): HashMap<String,MutableList<String>>{

        val ch_map = hashMapOf<String,MutableList<String>>()

            for(c in Sync.getOpenChannels()){
                val content = sync.waitContents(c).split(",")

                ch_map[c] = mutableListOf()

                if(content[0] != "null") {
                    for (p in content) {
                        ch_map[c]?.add(p)
                    }
                }

            }

        return ch_map
    }

    suspend fun resolveChannels(channels : List<String>) : HashMap<String,MutableList<String>>{

        val ch_map = hashMapOf<String,MutableList<String>>()

        for(c in channels){
            if(Sync.getOpenChannels().contains(c)){
                val content = sync.waitContents(c).split(",")

                ch_map[c] = mutableListOf()

                if(content[0] != "null") {
                    ch_map[c] = mutableListOf()
                    for (p in content) {
                        ch_map[c]?.add(p)
                    }
                }

            }
        }

        return ch_map
    }


}